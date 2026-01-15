package com.willowlabs.willowlabsnotifyservice.consumer;

import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import com.willowlabs.willowlabsnotifyservice.repository.NotificationRepository;
import com.willowlabs.willowlabsnotifyservice.service.handler.PushHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class PushNotificationConsumer {

    private final PushHandler pushHandler;
    private final NotificationRepository repository;

    @Bean
    public Consumer<Notification> pushConsumer() {
        return this::processNotification;
    }

    @Transactional
    public void processNotification(Notification notification) {
        log.info("Received PUSH notification with id={}", notification.getId());

        try {
            // Send push
            pushHandler.handle(notification);

            // Update status after successful send
            notification.setStatus(NotificationStatus.SENT);
            notification.setProcessedAt(LocalDateTime.now());
            repository.save(notification);

            log.info("PUSH notification {} processed and DB updated", notification.getId());

        } catch (Exception ex) {
            log.error("Failed to process PUSH notification {}", notification.getId(), ex);

            throw ex;
        }
    }
}
