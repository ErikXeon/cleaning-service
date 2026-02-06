package com.konalyan.cleaning.cleaning_service.dto;

public record UserSummaryResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}