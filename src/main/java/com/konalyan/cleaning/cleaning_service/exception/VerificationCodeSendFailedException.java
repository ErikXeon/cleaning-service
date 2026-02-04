package com.konalyan.cleaning.cleaning_service.exception;

public class VerificationCodeSendFailedException extends BusinessException {
    public VerificationCodeSendFailedException() {
        super(
                "VERIFICATION_SEND_FAILED",
                "Не удалось отправить код подтверждения. Попробуйте позже"
        );
    }
}

