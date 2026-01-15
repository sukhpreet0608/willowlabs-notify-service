package com.willowlabs.willowlabsnotifyservice.consumer;

import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import com.willowlabs.willowlabsnotifyservice.service.handler.EmailHandler;
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
class EmailNotificationConsumerTest {

    @Mock
    private EmailHandler emailHandler;

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private EmailNotificationConsumer emailNotificationConsumer;

    @Test
    @DisplayName("Should update status to SENT when email delivery succeeds")
    void testProcessNotificationSuccess() {
        // Arrange
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setStatus(NotificationStatus.PENDING);

        // Act
        emailNotificationConsumer.processNotification(notification);

        // Assert
        verify(emailHandler).handle(notification);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(savedNotification.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update status to FAILED when email delivery throws exception")
    void testProcessNotificationFailure() {
        // Arrange
        Notification notification = new Notification();
        notification.setId(2L);
        notification.setStatus(NotificationStatus.PENDING);

        // Simulate failure in the handler
        doThrow(new RuntimeException("SMTP Server Down"))
                .when(emailHandler).handle(any(Notification.class));

        // Act
        emailNotificationConsumer.processNotification(notification);

        // Assert
        verify(emailHandler).handle(notification);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(savedNotification.getProcessedAt()).isNotNull();
    }
}