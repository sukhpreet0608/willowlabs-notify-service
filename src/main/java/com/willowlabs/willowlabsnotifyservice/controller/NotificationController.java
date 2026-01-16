package com.willowlabs.willowlabsnotifyservice.controller;

import com.willowlabs.willowlabsnotifyservice.dto.NotificationRequest;
import com.willowlabs.willowlabsnotifyservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Use Case 1: Subscription (Trigger Internal Email)
     * Use Case 2: Scheduled Push (Targeted Mobile User)
     * Use Case 3: Subscription users SMS
     */
    @PostMapping
    public ResponseEntity<String> createNotification(@Valid @RequestBody NotificationRequest request) {
        notificationService.createNotification(request);
        return ResponseEntity.ok("Notification request accepted and persisted.");
    }
}