package com.willowlabs.willowlabsnotifyservice.dto;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for capturing incoming notification requests from the REST API.
 * @author Sukhpreet Khurana
 */

public record NotificationRequest(
        // Fields for Use Case 1 (Internal Email)
        @NotBlank(message = "Mobile user name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String mobileUserName,
        // Fields for Use Case 2 (Targeted Push)
        @NotBlank(message = "Device token is required for push notifications")
        String deviceToken, // FCM/APNs token provided by the app for Push
        // Fields for Use Case 3 (SMS)
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid mobile number format")
        String mobileNumber, // Optional: if provided, SMS logic is triggered
        String content, //optional
        boolean notifyAdmins     // Explicit flag to trigger Use Case 1
) {}
