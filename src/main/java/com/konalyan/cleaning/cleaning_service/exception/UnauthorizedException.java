package com.konalyan.cleaning.cleaning_service.exception;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException() {
        super("UNAUTHORIZED", "Вы не вошли в аккаунт");
    }
}
