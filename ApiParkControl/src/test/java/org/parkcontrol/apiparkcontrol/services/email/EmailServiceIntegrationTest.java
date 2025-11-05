package org.parkcontrol.apiparkcontrol.services.email;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    private String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        // Set up test email configuration
        ReflectionTestUtils.setField(emailService, "host", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailService, "port", "587");
        ReflectionTestUtils.setField(emailService, "username", "test@gmail.com");
        ReflectionTestUtils.setField(emailService, "password", "invalidpassword"); // Invalid for testing
    }

    @Test
    void testSendVerificationCode_InvalidCredentials_Integration() {
        // This test simulates invalid SMTP credentials
        String verificationCode = "123456";
        
        // Act & Assert - Should throw MessagingException due to invalid credentials
        assertThrows(MessagingException.class, () -> {
            emailService.sendVerificationCode(testEmail, verificationCode);
        });
    }

    @Test
    void testSendRecoveryCode_InvalidCredentials_Integration() {
        // This test simulates invalid SMTP credentials
        String recoveryCode = "654321";
        
        // Act & Assert - Should throw MessagingException due to invalid credentials
        assertThrows(MessagingException.class, () -> {
            emailService.sendRecoveryCode(testEmail, recoveryCode);
        });
    }

    @Test
    void testSendNotificationEmailEstadoGuia_InvalidCredentials_Integration() {
        // This test simulates invalid SMTP credentials
        String numeroGuia = "GU123456";
        String estado = "ENTREGADO";
        String nombreEmpresa = "Test Company";
        
        // Act & Assert - Should throw MessagingException due to invalid credentials
        assertThrows(MessagingException.class, () -> {
            emailService.sendNotificationEmailEstadoGuia(testEmail, numeroGuia, estado, nombreEmpresa);
        });
    }

    @Test
    void testEnviarEmailAsignacionRepartidor_InvalidCredentials_Integration() {
        // This test simulates invalid SMTP credentials
        String asunto = "Nueva asignación";
        String mensaje = "Test message";
        
        // Act & Assert - Should not throw exception (handles internally)
        assertDoesNotThrow(() -> {
            emailService.enviarEmailAsignacionRepartidor(testEmail, asunto, mensaje);
        });
    }

    @Test
    void testEnviarEmailGenerico_InvalidCredentials_Integration() {
        // This test simulates invalid SMTP credentials
        String asunto = "Mensaje genérico";
        String mensaje = "Test generic message";
        
        // Act & Assert - Should not throw exception (handles internally)
        assertDoesNotThrow(() -> {
            emailService.enviarEmailGenerico(testEmail, asunto, mensaje);
        });
    }

    @Test
    void testEmailService_Configuration_Integration() {
        // Test that the service is properly configured
        assertNotNull(emailService);
        
        // Verify that properties are injected
        String host = (String) ReflectionTestUtils.getField(emailService, "host");
        String port = (String) ReflectionTestUtils.getField(emailService, "port");
        String username = (String) ReflectionTestUtils.getField(emailService, "username");
        String password = (String) ReflectionTestUtils.getField(emailService, "password");
        
        assertNotNull(host);
        assertNotNull(port);
        assertNotNull(username);
        assertNotNull(password);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SMTP_TEST_ENABLED", matches = "true")
    void testSendVerificationCode_RealSMTP_Integration() throws MessagingException {
        // This test only runs if real SMTP testing is enabled
        // Set up real SMTP credentials through environment variables
        ReflectionTestUtils.setField(emailService, "host", System.getenv("SMTP_HOST"));
        ReflectionTestUtils.setField(emailService, "port", System.getenv("SMTP_PORT"));
        ReflectionTestUtils.setField(emailService, "username", System.getenv("SMTP_USERNAME"));
        ReflectionTestUtils.setField(emailService, "password", System.getenv("SMTP_PASSWORD"));
        
        String testRealEmail = System.getenv("TEST_EMAIL");
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            emailService.sendVerificationCode(testRealEmail, "123456");
        });
    }

    @Test
    void testEmailService_WithInvalidHost_Integration() {
        // Set invalid host
        ReflectionTestUtils.setField(emailService, "host", "invalid.host.com");
        ReflectionTestUtils.setField(emailService, "port", "587");
        ReflectionTestUtils.setField(emailService, "username", "test@example.com");
        ReflectionTestUtils.setField(emailService, "password", "password");
        
        // Act & Assert - Should throw MessagingException
        assertThrows(MessagingException.class, () -> {
            emailService.sendVerificationCode(testEmail, "123456");
        });
    }
/*
    @Test
    void testEmailService_WithInvalidPort_Integration() {
        // Set invalid port
        ReflectionTestUtils.setField(emailService, "host", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailService, "port", "999999");
        ReflectionTestUtils.setField(emailService, "username", "test@example.com");
        ReflectionTestUtils.setField(emailService, "password", "password");

        // Act & Assert - Should throw MessagingException
        assertThrows(MessagingException.class, () -> {
            emailService.sendRecoveryCode(testEmail, "654321");
        });
    }
*/
    @Test
    void testEmailService_LongContent_Integration() {
        // Test with very long content
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a very long message content that should test the limits of email sending. ");
        }
        
        String longAsunto = "Very Long Subject ".repeat(50);
        
        // Act & Assert - Should handle long content gracefully
        assertDoesNotThrow(() -> {
            emailService.enviarEmailGenerico(testEmail, longAsunto, longMessage.toString());
        });
    }

    @Test
    void testEmailService_SpecialCharacters_Integration() {
        // Test with special characters and Unicode
        String specialSubject = "Prueba con áéíóú ñ 中文 🚀 @#$%^&*()";
        String specialMessage = "Mensaje con caracteres especiales: áéíóú ñ 中文 🚀 @#$%^&*()";
        
        // Act & Assert - Should handle special characters gracefully
        assertDoesNotThrow(() -> {
            emailService.enviarEmailGenerico(testEmail, specialSubject, specialMessage);
        });
    }

    @Test
    void testEmailService_MultipleRecipients_Integration() {
        // Test with email that looks like multiple recipients (should still work)
        String multipleEmails = "test1@example.com,test2@example.com";
        
        // Act & Assert - Should handle gracefully
        assertDoesNotThrow(() -> {
            emailService.enviarEmailGenerico(multipleEmails, "Test Subject", "Test Message");
        });
    }

    @Test
    void testEmailService_EmptyContent_Integration() {
        // Test with empty content
        String emptySubject = "";
        String emptyMessage = "";
        
        // Act & Assert - Should handle empty content gracefully
        assertDoesNotThrow(() -> {
            emailService.enviarEmailGenerico(testEmail, emptySubject, emptyMessage);
        });
    }

   /* @Test
    void testEmailService_NullContent_Integration() {
        // Test with null content
        String nullSubject = null;
        String nullMessage = null;

        // Act & Assert - Should handle null content gracefully
        assertDoesNotThrow(() -> {
            emailService.enviarEmailGenerico(testEmail, nullSubject, nullMessage);
        });
    }*/

    @Test
    void testEmailService_ConcurrentSending_Integration() throws InterruptedException {
        // Test concurrent email sending
        Thread[] threads = new Thread[5];
        
        for (int i = 0; i < 5; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                assertDoesNotThrow(() -> {
                    emailService.enviarEmailGenerico(
                        testEmail, 
                        "Concurrent Test " + threadIndex, 
                        "Message from thread " + threadIndex
                    );
                });
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(5000); // 5 second timeout per thread
        }
        
        // All threads should complete without exceptions
        for (Thread thread : threads) {
            assertFalse(thread.isAlive());
        }
    }

    @Test
    void testEmailService_Performance_Integration() {
        // Test performance of email service methods
        long startTime = System.currentTimeMillis();
        
        // Send multiple emails sequentially
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            assertDoesNotThrow(() -> {
                emailService.enviarEmailGenerico(
                    testEmail, 
                    "Performance Test " + finalI,
                    "Performance message " + finalI
                );
            });
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time (even with failures, shouldn't hang)
        assertTrue(duration < 30000, "Email sending took too long: " + duration + "ms");
    }


}
