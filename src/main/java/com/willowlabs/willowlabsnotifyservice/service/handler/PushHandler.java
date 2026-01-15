package com.willowlabs.willowlabsnotifyservice.service.handler;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;

    /**
     * Concrete implementation for delivering mobile push notifications.
     * @author Sukhpreet Khurana
     */
    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class PushHandler implements  NotificationHandler {

        private final StreamBridge streamBridge;

        @Override
        public ChannelType  channel() {
            return ChannelType.PUSH;
        }

        @Override
        public void handle(Notification notification) {
            // In production, you would inject the FirebaseMessaging bean here.
            log.info("Processing push notification: {}", notification.getId());
            log.info("Processing push notification dispatched");
            log.info(">>>> [PUSH PROVIDER] Sending Mobile Alert to device: {}", notification.getRecipient());
            log.info("Content: {}", notification.getContent());
        }
    }
