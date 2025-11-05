package org.parkcontrol.apiparkcontrol.services.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceUnitTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private Session mockSession;

    private String testEmail = "test@example.com";
    private String testCode = "123456";

    @BeforeEach
    void setUp() {
        // Set up properties using ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "host", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailService, "port", "587");
        ReflectionTestUtils.setField(emailService, "username", "test@gmail.com");
        ReflectionTestUtils.setField(emailService, "password", "testpassword");
    }

    @Test
    void testSendVerificationCode_Success() throws Exception {
        // Arrange
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Act & Assert
            assertDoesNotThrow(() -> emailService.sendVerificationCode(testEmail, testCode));

            // Verify that Transport.send was called
            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void testSendVerificationCode_MessagingException() throws Exception {
        // Arrange
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("SMTP Error"));

            // Act & Assert
            MessagingException exception = assertThrows(MessagingException.class, () -> {
                emailService.sendVerificationCode(testEmail, testCode);
            });

            assertEquals("SMTP Error", exception.getMessage());
        }
    }

    @Test
    void testSendRecoveryCode_Success() throws Exception {
        // Arrange
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Act & Assert
            assertDoesNotThrow(() -> emailService.sendRecoveryCode(testEmail, testCode));

            // Verify that Transport.send was called
            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void testSendRecoveryCode_MessagingException() throws Exception {
        // Arrange
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Network timeout"));

            // Act & Assert
            MessagingException exception = assertThrows(MessagingException.class, () -> {
                emailService.sendRecoveryCode(testEmail, testCode);
            });

            assertEquals("Network timeout", exception.getMessage());
        }
    }

    @Test
    void testSendNotificationEmailEstadoGuia_Success() throws Exception {
        // Arrange
        String numeroGuia = "GU123456";
        String estado = "ENTREGADO";
        String nombreEmpresa = "Empresa Test";

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Act & Assert
            assertDoesNotThrow(() -> emailService.sendNotificationEmailEstadoGuia(
                    testEmail, numeroGuia, estado, nombreEmpresa));

            // Verify that Transport.send was called
            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void testSendNotificationEmailEstadoGuia_MessagingException() throws Exception {
        // Arrange
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Authentication failed"));

            // Act & Assert
            MessagingException exception = assertThrows(MessagingException.class, () -> {
                emailService.sendNotificationEmailEstadoGuia(testEmail, "GU123", "ENVIADO", "Test Company");
            });

            assertEquals("Authentication failed", exception.getMessage());
        }
    }

    @Test
    void testEnviarEmailAsignacionRepartidor_Success() {
        // Arrange
        String asunto = "Nueva asignación de repartidor";
        String mensaje = "Se le ha asignado una nueva entrega";

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Act & Assert - Should not throw any exception
            assertDoesNotThrow(() -> emailService.enviarEmailAsignacionRepartidor(testEmail, asunto, mensaje));

            // Verify that Transport.send was called
            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void testEnviarEmailAsignacionRepartidor_MessagingException() {
        // Arrange
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Server unavailable"));

            // Act & Assert - Should not throw exception, should handle internally
            assertDoesNotThrow(() -> emailService.enviarEmailAsignacionRepartidor(
                    testEmail, "Test Subject", "Test Message"));
        }
    }

    @Test
    void testEnviarEmailGenerico_Success() {
        // Arrange
        String asunto = "Mensaje genérico";
        String mensaje = "Este es un mensaje genérico de prueba";

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Act & Assert - Should not throw any exception
            assertDoesNotThrow(() -> emailService.enviarEmailGenerico(testEmail, asunto, mensaje));

            // Verify that Transport.send was called
            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void testEnviarEmailGenerico_MessagingException() {
        // Arrange
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Connection refused"));

            // Act & Assert - Should not throw exception, should handle internally
            assertDoesNotThrow(() -> emailService.enviarEmailGenerico(
                    testEmail, "Test Subject", "Test Message"));
        }
    }

    @Test
    void testEmailConfiguration_Properties() {
        // Test that email properties are correctly configured
        String host = (String) ReflectionTestUtils.getField(emailService, "host");
        String port = (String) ReflectionTestUtils.getField(emailService, "port");
        String username = (String) ReflectionTestUtils.getField(emailService, "username");
        String password = (String) ReflectionTestUtils.getField(emailService, "password");

        assertEquals("smtp.gmail.com", host);
        assertEquals("587", port);
        assertEquals("test@gmail.com", username);
        assertEquals("testpassword", password);
    }

    @Test
    void testSendVerificationCode_WithSpecialCharacters() throws Exception {
        // Arrange
        String specialEmail = "test+special@example.com";
        String specialCode = "12!@#$";

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Act & Assert
            assertDoesNotThrow(() -> emailService.sendVerificationCode(specialEmail, specialCode));

            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void testSendRecoveryCode_WithEmptyValues() throws Exception {
        // Arrange
        String emptyEmail = "";
        String emptyCode = "";

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Act & Assert
            assertDoesNotThrow(() -> emailService.sendRecoveryCode(emptyEmail, emptyCode));

            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void testSendNotificationEmailEstadoGuia_WithNullValues() throws Exception {
        // Arrange
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Act & Assert - Should handle null values gracefully
            assertDoesNotThrow(() -> emailService.sendNotificationEmailEstadoGuia(
                    testEmail, null, null, null));

            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void testMultipleConcurrentEmails() throws Exception {
        // Arrange
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Act - Send multiple emails
            assertDoesNotThrow(() -> {
                emailService.sendVerificationCode(testEmail, "111111");
                emailService.sendRecoveryCode(testEmail, "222222");
                emailService.enviarEmailGenerico(testEmail, "Test", "Test");
            });

            // Assert - Verify all emails were sent
            transportMock.verify(() -> Transport.send(any(Message.class)), times(3));
        }
    }

    @Test
    void testSessionCreation() throws Exception {
        // Arrange
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class);
             MockedStatic<Transport> transportMock = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> messageConstruction = mockConstruction(MimeMessage.class)) {

            sessionMock.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Act
            emailService.sendVerificationCode(testEmail, testCode);

            // Assert - Verify Session.getInstance was called
            sessionMock.verify(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)),
                    times(1));
        }
    }
}