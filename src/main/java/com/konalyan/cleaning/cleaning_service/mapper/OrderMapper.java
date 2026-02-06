package com.konalyan.cleaning.cleaning_service.mapper;

import com.konalyan.cleaning.cleaning_service.dto.CleaningServiceResponse;
import com.konalyan.cleaning.cleaning_service.dto.OrderResponse;
import com.konalyan.cleaning.cleaning_service.dto.UserSummaryResponse;
import com.konalyan.cleaning.cleaning_service.entity.CleaningService;
import com.konalyan.cleaning.cleaning_service.entity.Order;
import com.konalyan.cleaning.cleaning_service.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderResponse(
                order.getId(),
                toUserSummary(order.getClient()),
                toUserSummary(order.getCleaningStaff()),
                order.getDateTime(),
                order.getStatus(),
                order.getTotalPrice(),
                toServiceResponses(order.getServices()),
                order.getNotes(),
                order.getAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private UserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    private List<CleaningServiceResponse> toServiceResponses(List<CleaningService> services) {
        return Optional.ofNullable(services)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toServiceResponse)
                .toList();
    }

    private CleaningServiceResponse toServiceResponse(CleaningService service) {
        if (service == null) {
            return null;
        }
        return new CleaningServiceResponse(
                service.getId(),
                service.getName(),
                service.getPrice(),
                service.getDurationMinutes()
        );
    }
}