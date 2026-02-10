package com.konalyan.cleaning.cleaning_service.service;

import com.konalyan.cleaning.cleaning_service.dto.EmailNotification;
import com.konalyan.cleaning.cleaning_service.entity.User;
import com.konalyan.cleaning.cleaning_service.entity.VerificationCode;
import com.konalyan.cleaning.cleaning_service.exception.*;
import com.konalyan.cleaning.cleaning_service.repository.UserRepository;
import com.konalyan.cleaning.cleaning_service.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final KafkaProducerService kafkaProducerService;


    public String verify(String email, String codeValue) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        VerificationCode code = verificationRepository.findByUserAndUsedFalse(user)
                .orElseThrow(VerificationCodeNotFoundException::new);

        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            code.setUsed(true);
            verificationRepository.save(code);
            throw new VerificationCodeExpiredException();
        }

        if (code.getAttempts() >= 3) {
            code.setUsed(true);
            verificationRepository.save(code);
            throw new VerificationAttemptsExceededException();
        }

        if (!code.getCode().equals(codeValue)) {
            code.setAttempts(code.getAttempts() + 1);
            verificationRepository.save(code);
            throw new InvalidVerificationCodeException();
        }

        code.setUsed(true);
        user.setEnabled(true);

        verificationRepository.save(code);
        userRepository.save(user);
        log.info("Пользователь {} успешно подтверждён", email);
        return "Пользователь " + email + " успешно подтверждён";
    }

    public void resendCode(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        VerificationCode lastCode = verificationRepository
                .findTopByEmailOrderBySentAtDesc(email)
                .orElseThrow(VerificationCodeNotFoundException::new
                );

        if (lastCode.isUsed()) {
            throw new VerificationCodeAlreadyUsedException();
        }

        if (lastCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeExpiredException();
        }

        int resendDelaySec = 180;

        long secondsSinceLast =
                Duration.between(lastCode.getSentAt(), LocalDateTime.now()).getSeconds();

        if (secondsSinceLast < resendDelaySec) {
            long remaining = resendDelaySec - secondsSinceLast;
            throw new VerificationResendTooSoonException(remaining);
        }

        lastCode.setUsed(true);
        verificationRepository.save(lastCode);

        String newCode = codeGeneratorService.generateSixDigitCode();

        try {
            EmailNotification notification =
                    new EmailNotification(email, newCode);
            kafkaProducerService.sendVerificationCode(notification);
        } catch (Exception e) {
            throw new VerificationCodeSendFailedException();
        }

        VerificationCode code = new VerificationCode();
        code.setUser(user);
        code.setCode(newCode);
        code.setAttempts(0);
        code.setUsed(false);
        code.setExpiresAt(LocalDateTime.now().plusMinutes(3));
        code.setSentAt(LocalDateTime.now());
        code.setEmail(email);

        verificationRepository.save(code);
    }

}
