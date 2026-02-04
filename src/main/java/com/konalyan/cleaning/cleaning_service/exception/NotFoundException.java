package com.konalyan.cleaning.cleaning_service.exception;

public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super("NOTFOUND", message);
    }
}
