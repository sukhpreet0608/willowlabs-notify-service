package com.willowlabs.willowlabsnotifyservice.consumer;

import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import com.willowlabs.willowlabsnotifyservice.service.handler.PushHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationConsumerTest {

    @Mock
    private PushHandler pushHandler;

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private PushNotificationConsumer pushNotificationConsumer;

    @Test
    @DisplayName("Should update status to SENT when push delivery succeeds")
    void testProcessNotificationSuccess() {
        // Arrange
        Notification notification = Notification.builder()
                .id(101L)
                .recipient("device-token-abc")
                .status(NotificationStatus.SCHEDULED)
                .build();

        // Act
        pushNotificationConsumer.processNotification(notification);

        // Assert
        verify(pushHandler, times(1)).handle(notification);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update status to FAILED when push handler throws exception")
    void testProcessNotificationFailure() {
        // Arrange
        Notification notification = Notification.builder()
                .id(102L)
                .recipient("invalid-token")
                .status(NotificationStatus.SCHEDULED)
                .build();

        // Simulate a Push service failure (e.g., FCM/APNs error)
        doThrow(new RuntimeException("Push Service Unavailable"))
                .when(pushHandler).handle(any(Notification.class));

        // Act
        pushNotificationConsumer.processNotification(notification);

        // Assert
        verify(pushHandler).handle(notification);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(saved.getProcessedAt()).isNotNull();
        assertThat(saved.getId()).isEqualTo(102L);
    }
}