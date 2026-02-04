package com.konalyan.cleaning.cleaning_service.exception;

public class VerificationResendTooSoonException extends BusinessException {
    public VerificationResendTooSoonException(long seconds) {
        super(
                "VERIFICATION_RESEND_TOO_SOON",
                "Код уже отправлен. Попробуйте через " + seconds + " секунд"
        );
    }
}

