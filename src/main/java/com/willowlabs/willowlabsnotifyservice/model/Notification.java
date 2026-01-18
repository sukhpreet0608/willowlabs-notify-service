package com.willowlabs.willowlabsnotifyservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Persistence entity representing a notification record in the database.
 * @author Sukhpreet Khurana
 */
@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- ADDED FOR OPTIMISTIC LOCKING ---
    @Version
    private Integer version;

    private String recipient;

    private String mobileUserName;

    @Enumerated(EnumType.STRING)
    private ChannelType channel;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime scheduledAt;

    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
}