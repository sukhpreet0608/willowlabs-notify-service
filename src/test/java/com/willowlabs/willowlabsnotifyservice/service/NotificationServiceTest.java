package com.willowlabs.willowlabsnotifyservice.service;

import com.willowlabs.willowlabsnotifyservice.dto.NotificationRequest;
import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.InternalUser;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.InternalUserRepository;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private InternalUserRepository internalUserRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "pushDelayMinutes", 5);
    }

    @Test
    @DisplayName("Should orchestrate Email, Push, and SMS when all fields provided")
    void testCreateNotificationFullOrchestration() {
        // Given
        NotificationRequest request = new NotificationRequest(
                "Sukhpreet",
                "fcm_token_123",
                "+1234567890", // mobileNumber provided
                "Welcome to Willowlabs",
                true           // notifyAdmins true
        );

        InternalUser staff = new InternalUser();
        staff.setEmail("admin@willowlabs.com");

        when(internalUserRepository.findAllByIsDesignatedRecipientTrue())
                .thenReturn(List.of(staff));

        // When
        notificationService.createNotification(request);

        // Then
        // 1. Verify Email alerts (via saveAll)
        verify(notificationRepository, times(1)).saveAll(anyList());

        // 2. Verify Push and SMS (2 individual save calls)
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> capturedNotifications = captor.getAllValues();
        boolean hasPush = capturedNotifications.stream().anyMatch(n -> n.getChannel() == ChannelType.PUSH);
        boolean hasSms = capturedNotifications.stream().anyMatch(n -> n.getChannel() == ChannelType.SMS);

        assertThat(hasPush).isTrue();
        assertThat(hasSms).isTrue();
    }

    @Test
    @DisplayName("Should skip SMS if mobileNumber is null or blank")
    void testCreateNotificationSkipSms() {
        // Given
        NotificationRequest request = new NotificationRequest(
                "Sukhpreet", "token", null, "content", false
        );

        // When
        notificationService.createNotification(request);

        // Then
        // Only Push should be saved (1 call to save, 0 to saveAll since notifyAdmins is false)
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should skip email alerts if notifyAdmins is false")
    void testCreateNotificationSkipAdmins() {
        // Given
        NotificationRequest request = new NotificationRequest(
                "Sukhpreet", "token", null, "content", false
        );

        // When
        notificationService.createNotification(request);

        // Then
        verify(internalUserRepository, never()).findAllByIsDesignatedRecipientTrue();
        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should skip email alerts if no designated staff found")
    void testSendInternalAlertNoStaff() {
        // Given
        when(internalUserRepository.findAllByIsDesignatedRecipientTrue())
                .thenReturn(Collections.emptyList());

        // When
        notificationService.createNotification(new NotificationRequest("User", "token", null, "msg", true));

        // Then
        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should apply configured delay and correct recipient to scheduled push")
    void testSendScheduledPushWithDelay() {
        // Given
        NotificationRequest request = new NotificationRequest(
                "user1", "tokenX", null, "Welcome", false
        );
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // When
        notificationService.createNotification(request);

        // Then
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();

        assertThat(saved.getRecipient()).isEqualTo("tokenX");
        assertThat(saved.getChannel()).isEqualTo(ChannelType.PUSH);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SCHEDULED);
        assertThat(saved.getScheduledAt()).isAfter(java.time.LocalDateTime.now().plusMinutes(4));
    }
}