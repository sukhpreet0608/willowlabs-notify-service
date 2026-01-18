package com.willowlabs.willowlabsnotifyservice.service;

import com.willowlabs.willowlabsnotifyservice.dto.NotificationRequest;
import com.willowlabs.willowlabsnotifyservice.model.*;
import com.willowlabs.willowlabsnotifyservice.repository.InternalUserRepository;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orchestrates notification events using Transactional Outbox and Fan-out patterns.
 * Optimized with Concurrency Control for Multi-Instance Deployments.
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

        try {
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

            log.info("Transactional Outbox entries persisted successfully.");

        } catch (ObjectOptimisticLockingFailureException e) {
            // LOGIC: If another instance is processing the same request simultaneously,
            // we catch the exception to prevent a transaction rollback from affecting the user.
            log.warn("Concurrency conflict: Another instance is already processing notification for {}",
                    request.mobileUserName());
        }
    }

    public void sendInternalAlert(String userName) {
        List<InternalUser> staffMembers = internalUserRepository.findAllByIsDesignatedRecipientTrue();

        List<Notification> notifications = staffMembers.stream()
                .map(staff -> Notification.builder()
                        .recipient(staff.getEmail())
                        .mobileUserName(userName)
                        .content("System Alert: New subscriber - " + userName)
                        .channel(ChannelType.EMAIL)
                        .status(NotificationStatus.PENDING)
                        .scheduledAt(LocalDateTime.now())
                        .version(0) // Initialize version for new records
                        .build())
                .toList();

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
            log.info("Persisted {} EMAIL alerts with Version 0.", notifications.size());
        }
    }

    public void sendScheduledPush(NotificationRequest request) {
        notificationRepository.save(
                Notification.builder()
                        .recipient(request.deviceToken())
                        .content(request.content() != null ? request.content() : "Welcome " + request.mobileUserName() + " to Willowlabs")
                        .mobileUserName(request.mobileUserName())
                        .channel(ChannelType.PUSH)
                        .status(NotificationStatus.SCHEDULED)
                        .scheduledAt(LocalDateTime.now().plusMinutes(pushDelayMinutes))
                        .version(0) // Initialize version
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
                        .status(NotificationStatus.PENDING)
                        .scheduledAt(LocalDateTime.now())
                        .version(0) // Initialize version
                        .build()
        );
        log.info("Persisted SMS notification for: {}", request.mobileNumber());
    }
}