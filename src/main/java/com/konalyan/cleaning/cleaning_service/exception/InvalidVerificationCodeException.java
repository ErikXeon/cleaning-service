package com.konalyan.cleaning.cleaning_service.exception;

public class InvalidVerificationCodeException extends BusinessException {
    public InvalidVerificationCodeException() {
        super("INVALID_VERIFICATION_CODE", "Код подтверждения неверный");
    }
}

