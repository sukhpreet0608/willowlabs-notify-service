package com.willowlabs.willowlabsnotifyservice.service.handler;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class PushHandlerTest {

    @Mock
    private StreamBridge streamBridge; // Injected due to @RequiredArgsConstructor

    @InjectMocks
    private PushHandler pushHandler;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = Notification.builder()
                .id(1L)
                .recipient("device-token-123")
                .content("Test Push Content")
                .channel(ChannelType.PUSH)
                .build();
    }

    @Test
    @DisplayName("Should return PUSH as the supported channel")
    void channel_ReturnsPushType() {
        assertEquals(ChannelType.PUSH, pushHandler.channel());
    }

    @Test
    @DisplayName("Should execute push handling logic without errors")
    void handle_ProcessesSuccessfully() {
        // Since the current implementation only logs, we verify it doesn't crash.
        // In the future, when you add FirebaseMessaging, you would mock the Firebase bean
        // and verify the interaction here.
        assertDoesNotThrow(() -> pushHandler.handle(sampleNotification));
    }
}