package com.willowlabs.willowlabsnotifyservice.model;

/**
 * Represents the lifecycle stages of a notification from creation to final delivery.
 * @author Sukhpreet Khurana
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    PROCESSING,
    SCHEDULED
}
