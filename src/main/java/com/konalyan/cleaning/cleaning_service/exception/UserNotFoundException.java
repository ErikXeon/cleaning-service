package com.konalyan.cleaning.cleaning_service.exception;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String email) {
        super("USER_NOT_FOUND", "Пользователь с email " + email + " не найден");
    }

    public UserNotFoundException() {
        super("USER_NOT_FOUND", "Пользователь не найден");
    }
}
