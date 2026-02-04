package com.konalyan.cleaning.cleaning_service.exception;

public class VerificationAttemptsExceededException extends BusinessException {
    public VerificationAttemptsExceededException() {
        super("VERIFICATION_ATTEMPTS_EXCEEDED", "Превышено количество попыток подтверждения");
    }
}

