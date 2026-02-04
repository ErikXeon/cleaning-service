package com.konalyan.cleaning.cleaning_service.dto;

import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Set<String> roles
) {
}