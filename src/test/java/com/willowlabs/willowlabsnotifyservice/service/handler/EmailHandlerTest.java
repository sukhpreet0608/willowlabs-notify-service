package com.willowlabs.willowlabsnotifyservice.service.handler;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailHandlerTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailHandler emailHandler;

    @Test
    @DisplayName("Should return EMAIL as the supported channel")
    void channel_ReturnsEmailType() {
        assertEquals(ChannelType.EMAIL, emailHandler.channel());
    }

    @Test
    @DisplayName("Should construct and send a SimpleMailMessage correctly")
    void handle_SendsEmailSuccessfully() {
        // Arrange
        Notification notification = Notification.builder()
                .id(1L)
                .recipient("internal-user@willowlabs.com")
                .content("System Maintenance Alert")
                .channel(ChannelType.EMAIL)
                .build();

        // Act
        emailHandler.handle(notification);

        // Assert: Capture the message sent to mailSender
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        // Verify message details
        assertEquals("internal-user@willowlabs.com", capturedMessage.getTo()[0]);
        assertEquals("System Maintenance Alert", capturedMessage.getText());
        assertEquals("WillowLabs System Alert", capturedMessage.getSubject());
        assertEquals("no-reply@willowlabs.com", capturedMessage.getFrom());
    }

    @Test
    @DisplayName("Should log error but not crash when mail sender fails")
    void handle_HandlesMailSenderException() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("error@test.com")
                .build();

        // Force the mail sender to throw an exception
        doThrow(new RuntimeException("SMTP Connection Refused")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        // The handler has a try-catch block, so it should not propagate the exception
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> emailHandler.handle(notification));

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}