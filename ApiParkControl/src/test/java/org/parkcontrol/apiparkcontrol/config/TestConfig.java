package org.parkcontrol.apiparkcontrol.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.parkcontrol.apiparkcontrol.services.email.EmailService;

import static org.mockito.Mockito.*;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public EmailService mockEmailService() {
        EmailService emailService = mock(EmailService.class);
        
        try {
            // Mock the email methods to do nothing (successful execution)
            doNothing().when(emailService).sendVerificationCode(anyString(), anyString());
            doNothing().when(emailService).sendRecoveryCode(anyString(), anyString());
        } catch (Exception e) {
            // This shouldn't happen with mocks, but just in case
            throw new RuntimeException("Error setting up email service mock", e);
        }
        
        return emailService;
    }
}
