package com.konalyan.cleaning.cleaning_service.dto;

public record AssignRoleRequest(
        String email,
        String role
) {
}