package com.konalyan.cleaning.cleaning_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends BusinessException {
    public ForbiddenException() {
        super("FORBIDDEN", "Повторная отправка кода недоступна авторизованным пользователям");
    }
}

