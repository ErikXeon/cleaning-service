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
        log.info("Очистка началась: просроченные коды подтверждения");
        verificationCodeRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Очистка завершена: просроченные коды подтверждения");
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * MON")
    public void cleanOldLoginAttempts() {
        log.info("Очистка началась: старые попытки входа");
        loginAttemptRepository.deleteByLastAttemptBefore(
                LocalDateTime.now().minusDays(7)
        );
        log.info("Очистка завершена: старые попытки входа");
    }


}
