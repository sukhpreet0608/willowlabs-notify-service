package com.willowlabs.willowlabsnotifyservice.service;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationProcessor processor;

    @InjectMocks
    private NotificationScheduler scheduler;

    @Test
    @DisplayName("Should process all due notifications successfully")
    void processOutbox_Success() {
        Notification n1 = Notification.builder().id(1L).channel(ChannelType.EMAIL).recipient("user1@test.com").build();
        Notification n2 = Notification.builder().id(2L).channel(ChannelType.PUSH).recipient("user2").build();

        when(repository.findAllByStatusInAndScheduledAtBefore(any(), any()))
                .thenReturn(List.of(n1, n2));

        scheduler.processOutbox();

        verify(processor, times(1)).processSingle(n1);
        verify(processor, times(1)).processSingle(n2);
        verify(repository, times(1)).findAllByStatusInAndScheduledAtBefore(
                eq(List.of(NotificationStatus.PENDING, NotificationStatus.SCHEDULED)),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("Should continue processing next notifications even if one fails")
    void processOutbox_PartialFailure() {
        Notification nFail = Notification.builder().id(1L).recipient("fail").build();
        Notification nSuccess = Notification.builder().id(2L).recipient("success").build();

        when(repository.findAllByStatusInAndScheduledAtBefore(any(), any()))
                .thenReturn(List.of(nFail, nSuccess));

        doThrow(new RuntimeException("Processing error")).when(processor).processSingle(nFail);

        scheduler.processOutbox();

        verify(processor, times(1)).processSingle(nFail);
        verify(processor, times(1)).processSingle(nSuccess); // Verified that it continued to the next item
    }

    @Test
    @DisplayName("Should do nothing when no notifications are due")
    void processOutbox_EmptyList() {
        when(repository.findAllByStatusInAndScheduledAtBefore(any(), any()))
                .thenReturn(Collections.emptyList());

        scheduler.processOutbox();

        verifyNoInteractions(processor);
    }
}