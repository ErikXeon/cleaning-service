package com.konalyan.cleaning.cleaning_service.dto;

public record LoginResponse(
        String message,
        UserResponse user
) {
}
