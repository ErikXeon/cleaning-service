package com.konalyan.cleaning.cleaning_service.dto;

import com.konalyan.cleaning.cleaning_service.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        UserSummaryResponse client,
        UserSummaryResponse cleaningStaff,
        LocalDateTime dateTime,
        OrderStatus status,
        BigDecimal totalPrice,
        List<CleaningServiceResponse> services,
        String notes,
        String address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}