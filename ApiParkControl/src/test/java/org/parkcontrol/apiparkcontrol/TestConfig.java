package org.parkcontrol.apiparkcontrol;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.parkcontrol.apiparkcontrol.utils.Encriptation;

import static org.mockito.Mockito.*;

@TestConfiguration
@Profile("test")
public class TestConfig {  // ← cambió el nombre de la clase

    @Bean
    @Primary
    public Encriptation testEncriptation() {
        Encriptation encriptation = mock(Encriptation.class);

        when(encriptation.encrypt(anyString())).thenAnswer(invocation -> {
            String password = invocation.getArgument(0);
            return "$2a$10$" + password.hashCode();
        });

        when(encriptation.matches(anyString(), anyString())).thenAnswer(invocation -> {
            String rawPassword = invocation.getArgument(0);
            String encodedPassword = invocation.getArgument(1);
            return encodedPassword.equals("$2a$10$" + rawPassword.hashCode()) ||
                    (rawPassword.equals("password123") && encodedPassword.equals("$2a$10$N2l0Ld5Y.x8Y.wD0tJF7zuO2f3wGgFg0XlKkJmxU/M5LgMzOxM8J2"));
        });

        return encriptation;
    }
}
