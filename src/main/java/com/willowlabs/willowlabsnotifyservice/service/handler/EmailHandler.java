package com.willowlabs.willowlabsnotifyservice.service.handler;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation for delivering notifications via SMTP/Email.
 * @author Sukhpreet Khurana
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailHandler implements  NotificationHandler {
    private final JavaMailSender mailSender;
    @Override
    public ChannelType  channel() {
        return ChannelType.EMAIL;
    }

    @Override
    public void handle(Notification notification) {
        try {
            log.info("Sending dynamic email to internal recipient: {}", notification.getRecipient());

            SimpleMailMessage message = new SimpleMailMessage();

            // Recipient and Content come directly from the DB record
            message.setTo(notification.getRecipient());
            message.setText(notification.getContent());

            // You can also pull the subject from a config file or a field in the entity
            message.setSubject("WillowLabs System Alert");
            message.setFrom("no-reply@willowlabs.com");

            mailSender.send(message);

            log.info("Successfully sent email ID: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", notification.getRecipient(), e.getMessage());
        }
    }
}
