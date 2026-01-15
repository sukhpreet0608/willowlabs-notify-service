package com.willowlabs.willowlabsnotifyservice.service;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProcessor {

    private final NotificationRepository repository;
    private final StreamBridge streamBridge;

    /**
     * Processes a single notification in its own transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingle(Notification notification) {
        log.debug("Processing {} notification for recipient: {}",
                notification.getChannel(), notification.getRecipient());

        String routingKey;

        // Determine routingKey based on channel type
        switch (notification.getChannel()) {
            case PUSH -> routingKey = "notification.push";
            case EMAIL -> routingKey = "notification.email";
            case SMS -> routingKey = "notification.sms";
            default -> {
                log.error("Unknown channel type for notification {}", notification.getId());
                return; // skip unknown channel types
            }
        }

        // Send to the correct queue via StreamBridge
        boolean sent = streamBridge.send(
                "notification-out-0",
                MessageBuilder.withPayload(notification)
                        .setHeader("routingKey", routingKey)
                        .build()
        );

        if (!sent) {
            throw new RuntimeException("Failed to publish notification: " + notification.getId());
        }

        // Update DB after successful send
        notification.setStatus(NotificationStatus.SENT);
        notification.setProcessedAt(LocalDateTime.now());
        repository.save(notification);

        log.info("Notification {} sent via {} and DB updated", notification.getId(), notification.getChannel());
    }
}
