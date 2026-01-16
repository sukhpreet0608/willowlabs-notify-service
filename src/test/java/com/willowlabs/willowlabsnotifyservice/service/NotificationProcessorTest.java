package com.willowlabs.willowlabsnotifyservice.service;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProcessorTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private NotificationProcessor processor;

    @Test
    @DisplayName("Should send EMAIL notification with correct routing key and update status")
    void processSingle_Email_Success() {
        Notification notification = Notification.builder()
                .id(100L)
                .channel(ChannelType.EMAIL)
                .recipient("staff@willowlabs.com")
                .status(NotificationStatus.PENDING)
                .build();

        when(streamBridge.send(eq("notification-out-0"), any(Message.class))).thenReturn(true);

        processor.processSingle(notification);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge).send(eq("notification-out-0"), messageCaptor.capture());

        Message sentMessage = messageCaptor.getValue();
        assertEquals("notification.email", sentMessage.getHeaders().get("routingKey"));
        assertEquals(notification, sentMessage.getPayload());

        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertNotNull(notification.getProcessedAt());
        verify(repository).save(notification);
    }

    @Test
    @DisplayName("Should send PUSH notification with correct routing key")
    void processSingle_Push_Success() {
        Notification notification = Notification.builder()
                .id(101L)
                .channel(ChannelType.PUSH)
                .status(NotificationStatus.SCHEDULED)
                .build();

        when(streamBridge.send(anyString(), any(Message.class))).thenReturn(true);

        processor.processSingle(notification);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge).send(anyString(), messageCaptor.capture());
        assertEquals("notification.push", messageCaptor.getValue().getHeaders().get("routingKey"));
    }

    @Test
    @DisplayName("Should throw exception and not update DB if StreamBridge fails")
    void processSingle_PublishFailure() {
        Notification notification = Notification.builder()
                .id(500L)
                .channel(ChannelType.SMS)
                .status(NotificationStatus.PENDING)
                .build();

        // Simulate StreamBridge returning false (failure to send)
        when(streamBridge.send(anyString(), any(Message.class))).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            processor.processSingle(notification);
        });

        assertTrue(exception.getMessage().contains("Failed to publish"));

        verify(repository, never()).save(any());
        assertNotEquals(NotificationStatus.SENT, notification.getStatus());
    }
}