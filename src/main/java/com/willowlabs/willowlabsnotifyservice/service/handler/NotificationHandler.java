package com.willowlabs.willowlabsnotifyservice.service.handler;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;

/**
 * Common interface for all notification delivery providers.
 * @author Sukhpreet Khurana
 */
public interface NotificationHandler {
    ChannelType  channel();
    void handle(Notification notification);
}
