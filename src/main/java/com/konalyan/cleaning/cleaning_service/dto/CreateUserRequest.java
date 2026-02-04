package com.konalyan.cleaning.cleaning_service.dto;

public record CreateUserRequest(
        String email,
        String password,
        String firstName,
        String lastName
){
}