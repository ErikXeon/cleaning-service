package com.konalyan.cleaning.cleaning_service.service;

import com.konalyan.cleaning.cleaning_service.repository.LoginAttemptRepository;
import com.konalyan.cleaning.cleaning_service.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final LoginAttemptRepository loginAttemptRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiredVerificationCodes() {
        log.info("Cleanup started: expired verification codes");
        verificationCodeRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleanup finished: expired verification codes");
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * MON")
    public void cleanOldLoginAttempts() {
        log.info("Cleanup started: old login attempts");
        loginAttemptRepository.deleteByLastAttemptBefore(
                LocalDateTime.now().minusDays(7)
        );
        log.info("Cleanup finished: old login attempts");
    }


}
