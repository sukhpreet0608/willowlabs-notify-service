package com.willowlabs.willowlabsnotifyservice.repository;

import com.willowlabs.willowlabsnotifyservice.model.AudienceType;
import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Standard JPA repository for performing CRUD operations on Notification entities.
 * @author Sukhpreet Khurana
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Finds emails that are PENDING and due to be sent
    List<Notification> findByStatusAndChannelAndScheduledAtBefore(
            NotificationStatus status,
            ChannelType channel,
            LocalDateTime now
    );

    /**
     * Finds all notifications that need processing based on:
     * 1. Status: Must be in the provided list (e.g., PENDING, SCHEDULED)
     * 2. Time: scheduledAt must be less than or equal to the current time
     */
    List<Notification> findAllByStatusInAndScheduledAtBefore(
            Collection<NotificationStatus> statuses,
            LocalDateTime dateTime
    );
}
