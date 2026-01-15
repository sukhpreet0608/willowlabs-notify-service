package com.willowlabs.willowlabsnotifyservice.consumer;

import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeadLetterListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private DeadLetterListener deadLetterListener;

    @Test
    @DisplayName("Should update status to FAILED when a message lands in Push DLQ")
    void testHandlePushFailure() {
        // Given
        Long notificationId = 101L;
        Notification mockNotification = Notification.builder()
                .id(notificationId)
                .status(NotificationStatus.PENDING)
                .build();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(mockNotification));

        // When
        deadLetterListener.handlePushFailure(mockNotification);

        // Then
        verify(notificationRepository, times(1)).save(argThat(notification ->
                notification.getStatus() == NotificationStatus.FAILED &&
                        notification.getId().equals(notificationId)
        ));
    }

    @Test
    @DisplayName("Should log error but not fail if notification ID is not found in DB")
    void testHandleFailureWithMissingRecord() {
        // Given
        Notification mockNotification = Notification.builder().id(999L).build();
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        deadLetterListener.handleEmailFailure(mockNotification);

        // Then
        verify(notificationRepository, never()).save(any());
    }
}