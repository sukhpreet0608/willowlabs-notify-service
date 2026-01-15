package com.willowlabs.willowlabsnotifyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.willowlabs.willowlabsnotifyservice.dto.NotificationRequest;
import com.willowlabs.willowlabsnotifyservice.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/notifications - Success Scenario")
    void shouldAcceptNotificationRequest() throws Exception {
        // Given: Using the Builder for your Record
        NotificationRequest request = new NotificationRequest(
                "Sukhpreet",
                "fcm-token-123",
                null,
                "Test Content",
                true);

// Convert record to JSON string manually
        String jsonRequest = objectMapper.writeValueAsString(request);
        // When & Then: Using fluent AssertJ-style API
        assertThat(mvc.post()
                .uri("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)) // MockMvcTester automatically serializes objects
                .hasStatusOk()
                .bodyText().isEqualTo("Notification request accepted and persisted.");

        // Verify service was called
        verify(notificationService).createNotification(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/notifications - Validation Failure (Empty Name)")
    void shouldReturn400WhenNameIsEmpty() throws Exception {
        // Given: Invalid request (missing mobileUserName)
        NotificationRequest invalidRequest = new NotificationRequest(
                null,
                "fcm-token-123",
                null,
                "Test Content",
                true);
// Convert record to JSON string manually
        String jsonRequest = objectMapper.writeValueAsString(invalidRequest);
        // When & Then
        assertThat(mvc.post()
                .uri("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .hasStatus4xxClientError();
    }
}