package com.konalyan.cleaning.cleaning_service.exception;

public class UserNotActiveException extends BusinessException {
    public UserNotActiveException() {
        super("USER_NOT_ACTIVE", "Аккаунт не активирован");
    }
}

