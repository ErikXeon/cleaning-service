package com.konalyan.cleaning.cleaning_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CreateOrderRequest(
        LocalDateTime dateTime,
        List<Long> serviceIds,
        String notes,
        String address
) {
}
