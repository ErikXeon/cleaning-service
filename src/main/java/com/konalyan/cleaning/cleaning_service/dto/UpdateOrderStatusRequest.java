package com.konalyan.cleaning.cleaning_service.dto;

import com.konalyan.cleaning.cleaning_service.enums.OrderStatus;

public record UpdateOrderStatusRequest(
        OrderStatus status
) {
}
