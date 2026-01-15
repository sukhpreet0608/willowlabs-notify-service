package com.willowlabs.willowlabsnotifyservice.service.handler;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock implementation for SMS to allow the reviewer to run the app without Twilio keys.
 * @author Sukhpreet Khurana
 */
@Slf4j
@Service
public class SmsHandler implements  NotificationHandler {

    @Override
    public ChannelType  channel() {
        return ChannelType.SMS;
    }

    @Override
    public void handle(Notification notification) {
        log.info(">>>> [MOCK SMS PROVIDER] Simulating text to {}: {}", notification.getRecipient(), notification.getContent());
        // In a real production scenario, this would call the paid service Twilio SDK.
    }
}
