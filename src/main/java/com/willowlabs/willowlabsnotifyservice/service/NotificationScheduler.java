package com.willowlabs.willowlabsnotifyservice.service;

import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Universal Outbox Relay.
 * Handles immediate internal emails and scheduled mobile push notifications.
 * @author Sukhpreet Khurana
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository repository;
    private final NotificationProcessor processor;
    /**
     * Runs every 2 seconds (2000ms).
     * 1. Emails: Status=PENDING, ScheduledAt=Now (Picked up almost instantly).
     * 2. Push: Status=SCHEDULED, ScheduledAt=Future (Picked up only when time passes).
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processOutbox() {
        LocalDateTime now = LocalDateTime.now();

        // Fetch everything due: Internal Emails (PENDING) and Mobile Push (SCHEDULED)
        List<Notification> dueNotifications = repository.findAllByStatusInAndScheduledAtBefore(
                List.of(NotificationStatus.PENDING, NotificationStatus.SCHEDULED),
                now
        );

        if (dueNotifications.isEmpty()) {
            log.info("[Outbox Relay] No notifications due for processing.");
            return;
        }

        log.info("[Outbox Relay] Found {} notifications to process.", dueNotifications.size());
        int successCount = 0;
        int failureCount = 0;
        for (Notification notification : dueNotifications) {
            try {
                log.info("[Outbox Relay] Processing ID: {} | Channel: {} | Recipient: {}",
                        notification.getId(), notification.getChannel(), notification.getRecipient());
                // Each call happens in its own fresh transaction
                processor.processSingle(notification);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to process ID {}: {}", notification.getId(), e.getMessage());
                // The loop continues to the next record!
            }
        }
        log.info("Scheduler Cycle complete. Success: {}, Failures: {}", successCount, failureCount);
    }
}