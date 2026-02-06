package com.konalyan.cleaning.cleaning_service.dto;

import java.math.BigDecimal;

public record CleaningServiceResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer durationMinutes
) {
}