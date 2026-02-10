package com.konalyan.cleaning.cleaning_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyCodeRequest(
        @Email
        @Email(message = "Некорректный формат email")
        @NotBlank(message = "Поле email обязательно")
        String email,

        @NotBlank
        @Size(min = 6, max = 6)
        @NotBlank(message = "Код подтверждения обязателен")
        @Size(min = 6, max = 6, message = "Код подтверждения должен содержать 6 символов")
        String code
) {
}