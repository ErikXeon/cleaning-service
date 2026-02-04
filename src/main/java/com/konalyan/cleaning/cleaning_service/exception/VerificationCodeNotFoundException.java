package com.konalyan.cleaning.cleaning_service.exception;

public class VerificationCodeNotFoundException extends BusinessException {
    public VerificationCodeNotFoundException() {
        super("VERIFICATION_CODE_NOT_FOUND", "Код подтверждения не найден");
    }
}

