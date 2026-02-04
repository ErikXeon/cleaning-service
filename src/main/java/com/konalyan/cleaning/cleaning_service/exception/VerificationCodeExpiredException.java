package com.konalyan.cleaning.cleaning_service.exception;

public class VerificationCodeExpiredException extends BusinessException {
    public VerificationCodeExpiredException() {
        super("VERIFICATION_CODE_EXPIRED", "Срок действия кода истёк");
    }
}
