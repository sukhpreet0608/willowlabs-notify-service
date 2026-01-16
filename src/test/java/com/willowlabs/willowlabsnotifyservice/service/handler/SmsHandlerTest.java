package com.willowlabs.willowlabsnotifyservice.service.handler;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SmsHandlerTest {

    private SmsHandler smsHandler;

    @BeforeEach
    void setUp() {
        // Since there are no constructor dependencies, we can instantiate directly
        smsHandler = new SmsHandler();
    }

    @Test
    @DisplayName("Should return SMS as the supported channel")
    void channel_ReturnsSmsType() {
        assertEquals(ChannelType.SMS, smsHandler.channel());
    }

    @Test
    @DisplayName("Should execute SMS mock logic without throwing exceptions")
    void handle_ProcessesSuccessfully() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("9876543210")
                .content("Your OTP is 1234")
                .channel(ChannelType.SMS)
                .build();

        // Act & Assert
        // We verify that the method completes without error, which confirms the logging logic is safe
        assertDoesNotThrow(() -> smsHandler.handle(notification));
    }
}