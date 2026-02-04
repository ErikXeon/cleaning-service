package com.konalyan.cleaning.cleaning_service.exception;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String email) {
        super("USER_ALREADY_EXISTS", "Пользователь с email " + email + " уже зарегистрирован");
    }
}
