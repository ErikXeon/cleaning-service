package com.konalyan.cleaning.cleaning_service.exception;

public class BadRequest extends BusinessException {
    public BadRequest(String message) {
        super("BAD_REQUEST", message);
    }
}
