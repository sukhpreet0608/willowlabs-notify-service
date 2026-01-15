package com.willowlabs.willowlabsnotifyservice.consumer;

import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Technical Highlight: The "Final Failure" Handler.
 * This class listens to the Dead Letter Queues (DLQ). When a notification
 * exhausts all retry attempts (max-attempts), it lands here.
 * This ensures the Database source-of-truth stays in sync with RabbitMQ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeadLetterListener {

    private final NotificationRepository notificationRepository;

    /**
     * Listens to the DLQ specifically for the Push group.
     * Queue name pattern: {destination}.{group}.dlq
     */
    @RabbitListener(queues = "notification-push.push-group.dlq")
    @Transactional
    public void handlePushFailure(Notification failedNotification) {
        log.error("CRITICAL: Push notification {} failed after all retries. Marking as FAILED in DB.",
                failedNotification.getId());

        updateNotificationStatus(failedNotification.getId());
    }

    @RabbitListener(queues = "notification-email.email-group.dlq")
    @Transactional
    public void handleEmailFailure(Notification failedNotification) {
        log.error("CRITICAL: Email notification {} failed after all retries. Marking as FAILED in DB.",
                failedNotification.getId());

        updateNotificationStatus(failedNotification.getId());
    }

    @RabbitListener(queues = "notification-sms.sms-group.dlq")
    @Transactional
    public void handleSmsFailure(Notification failedNotification) {
        log.error("CRITICAL: SMS notification {} failed after all retries. Marking as FAILED in DB.",
                failedNotification.getId());

        updateNotificationStatus(failedNotification.getId());
    }

    private void updateNotificationStatus(Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            log.info("Database updated: Notification {} is now FAILED.", id);
        });
    }
}