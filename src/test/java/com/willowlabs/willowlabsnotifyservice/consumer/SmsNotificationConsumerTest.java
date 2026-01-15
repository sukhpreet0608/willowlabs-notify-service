package com.willowlabs.willowlabsnotifyservice.consumer;

import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import com.willowlabs.willowlabsnotifyservice.service.handler.SmsHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsNotificationConsumerTest {

    @Mock
    private SmsHandler smsHandler;

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private SmsNotificationConsumer smsNotificationConsumer;

    @Test
    @DisplayName("Should update status to SENT and set processedAt when SMS delivery succeeds")
    void shouldProcessSmsSuccessfully() {
        // Arrange
        Notification notification = new Notification();
        notification.setId(500L);
        notification.setRecipient("+1234567890");
        notification.setStatus(NotificationStatus.PENDING);

        // Act
        smsNotificationConsumer.processNotification(notification);

        // Assert
        verify(smsHandler, times(1)).handle(notification);

        // Capture the notification saved to the DB
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getProcessedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(saved.getProcessedAt()).isAfter(LocalDateTime.now().minusSeconds(5));
    }

    @Test
    @DisplayName("Should update status to FAILED when SMS handler throws an exception")
    void shouldMarkAsFailedWhenSmsHandlerFails() {
        // Arrange
        Notification notification = new Notification();
        notification.setId(501L);
        notification.setStatus(NotificationStatus.PENDING);

        // Simulate an SMS provider error (e.g., Twilio/AWS SNS failure)
        doThrow(new RuntimeException("SMS Provider Timeout"))
                .when(smsHandler).handle(any(Notification.class));

        // Act
        smsNotificationConsumer.processNotification(notification);

        // Assert
        verify(smsHandler).handle(notification);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(saved.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should verify the bean returns a non-null Consumer")
    void shouldReturnValidConsumerBean() {
        assertThat(smsNotificationConsumer.smsConsumer()).isNotNull();
    }
}