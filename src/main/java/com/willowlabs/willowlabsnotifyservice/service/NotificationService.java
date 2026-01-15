package com.willowlabs.willowlabsnotifyservice.service;

import com.willowlabs.willowlabsnotifyservice.dto.NotificationRequest;
import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.InternalUser;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.InternalUserRepository;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orchestrates notification events using Transactional Outbox and Fan-out patterns.
 * Ensures reliable delivery of internal alerts and scheduled mobile pushes.
 * @author Sukhpreet Khurana
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final InternalUserRepository internalUserRepository;
    @Value("${notification.push.delay-minutes:0}")
    private int pushDelayMinutes;

    @Transactional
    public void createNotification(NotificationRequest request) {
        log.info("Processing notification request for user: {}", request.mobileUserName());

        // 1. Conditional Internal Alert (Email)
        if (request.notifyAdmins()) {
            this.sendInternalAlert(request.mobileUserName());
        }

        // 2. Mobile Push Notification
        this.sendScheduledPush(request);

        // 3. Conditional SMS Notification
        if (request.mobileNumber() != null && !request.mobileNumber().isBlank()) {
            this.sendSms(request);
        }

        log.info("Transactional Outbox entries persisted successfully for all applicable channels.");
    }

    public void sendInternalAlert(String userName) {
// 1. Fetch all designated staff
        List<InternalUser> staffMembers = internalUserRepository.findAllByIsDesignatedRecipientTrue();

        // 2. Map the staff list into a list of Notification entities
        List<Notification> notifications = staffMembers.stream()
                .map(staff -> Notification.builder()
                        .recipient(staff.getEmail())
                        .mobileUserName(userName)
                        .content("System Alert: New subscriber - " + userName)
                        .channel(ChannelType.EMAIL)
                        .status(NotificationStatus.PENDING)
                        .scheduledAt(LocalDateTime.now())
                        .build())
                .toList();

        // 3. Perform a single batch save
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
            log.info("Persisted {} EMAIL alerts to the Outbox table.", notifications.size());
        }
    }

    public void sendScheduledPush(NotificationRequest request) {
        // PERSISTENCE FOR USE CASE 2
        notificationRepository.save(
                Notification.builder()
                        .recipient(request.deviceToken())
                        .content(request.content() != null ? request.content() : "Welcome " + request.mobileUserName() + " to Willowlabs")
                        .mobileUserName(request.mobileUserName())
                        .channel(ChannelType.PUSH)
                        .status(NotificationStatus.SCHEDULED)
                        .scheduledAt(LocalDateTime.now().plusMinutes(pushDelayMinutes))
                        .build()
        );
    }

    private void sendSms(NotificationRequest request) {
        notificationRepository.save(
                Notification.builder()
                        .recipient(request.mobileNumber())
                        .content("Hello " + request.mobileUserName() + ", welcome to Willowlabs!")
                        .mobileUserName(request.mobileUserName())
                        .channel(ChannelType.SMS)
                        .status(NotificationStatus.PENDING) // Usually SMS is sent immediately
                        .scheduledAt(LocalDateTime.now())
                        .build()
        );
        log.info("Persisted SMS notification to the Outbox for number: {}", request.mobileNumber());
    }
}