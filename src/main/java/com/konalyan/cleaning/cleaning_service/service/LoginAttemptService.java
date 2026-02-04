package com.konalyan.cleaning.cleaning_service.service;

import com.konalyan.cleaning.cleaning_service.entity.LoginAttempt;
import com.konalyan.cleaning.cleaning_service.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_MINUTES = 15;

    public void loginSucceeded(String email) {
        loginAttemptRepository.findByEmail(email).ifPresent(attempt -> {
            attempt.setAttempts(0);
            attempt.setBlocked(false);
            attempt.setBlockedUntil(null);
            loginAttemptRepository.save(attempt);
            log.info("Login successful, attempts reset for {}", email);
        });
    }

    public void loginFailed(String email) {
        LoginAttempt attempt = loginAttemptRepository.findByEmail(email)
                .orElse(LoginAttempt.builder()
                        .email(email)
                        .attempts(0)
                        .blocked(false)
                        .build());

        if (attempt.isBlocked() && attempt.getBlockedUntil() != null &&
                attempt.getBlockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("Blocked login attempt for {}", email);
            return;
        }

        attempt.setAttempts(attempt.getAttempts() + 1);
        attempt.setLastAttempt(LocalDateTime.now());

        if (attempt.getAttempts() >= MAX_ATTEMPTS) {
            attempt.setBlocked(true);
            attempt.setBlockedUntil(LocalDateTime.now().plusMinutes(BLOCK_MINUTES));
            log.warn("User {} blocked due to too many failed attempts", email);
        }

        loginAttemptRepository.save(attempt);
    }

    public boolean isBlocked(String email) {
        return loginAttemptRepository.findByEmail(email)
                .map(attempt -> attempt.isBlocked() &&
                        attempt.getBlockedUntil() != null &&
                        attempt.getBlockedUntil().isAfter(LocalDateTime.now()))
                .orElse(false);
    }
}
