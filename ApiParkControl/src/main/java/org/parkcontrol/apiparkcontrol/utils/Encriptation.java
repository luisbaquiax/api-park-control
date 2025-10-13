package org.parkcontrol.apiparkcontrol.utils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Encriptation {
    private PasswordEncoder passwordEncoder;

    public Encriptation() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String encrypt(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
