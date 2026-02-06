package com.konalyan.cleaning.cleaning_service.exception;

public class BadCredentialsException extends BusinessException {
    public BadCredentialsException() {
        super("BAD_CREDENTIALS", "Неверный логин или пароль");
    }
}