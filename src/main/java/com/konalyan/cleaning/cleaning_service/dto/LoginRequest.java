package com.konalyan.cleaning.cleaning_service.dto;

public record LoginRequest(
        String email,
        String password
) {
}
