package com.konalyan.cleaning.cleaning_service.dto;

import java.math.BigDecimal;

public record UpdateServicePriceRequest(
        BigDecimal price
) {
}