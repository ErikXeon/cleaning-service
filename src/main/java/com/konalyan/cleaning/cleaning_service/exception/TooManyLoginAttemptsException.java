package com.konalyan.cleaning.cleaning_service.exception;

public class TooManyLoginAttemptsException extends BusinessException {
    public TooManyLoginAttemptsException() {
        super("TOO_MANY_LOGIN_ATTEMPTS", "Слишком много попыток входа. Попробуйте позже");
    }
}