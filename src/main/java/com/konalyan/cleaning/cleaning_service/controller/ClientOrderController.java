package com.konalyan.cleaning.cleaning_service.controller;

import com.konalyan.cleaning.cleaning_service.dto.CreateOrderRequest;
import com.konalyan.cleaning.cleaning_service.dto.OrderResponse;
import com.konalyan.cleaning.cleaning_service.entity.Order;
import com.konalyan.cleaning.cleaning_service.dto.MessageResponse;
import com.konalyan.cleaning.cleaning_service.mapper.OrderMapper;
import com.konalyan.cleaning.cleaning_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientOrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    // ------------------- ЛК клиента -------------------

    @PreAuthorize("hasRole('CLIENT') and !hasRole('MANAGER')")
    @PostMapping("/orders")
    public Order createOrder(@RequestBody CreateOrderRequest request,
                              Authentication authentication) {
        return orderService.createOrder(
                authentication.getName(),
                request.dateTime(),
                request.serviceIds(),
                request.notes(),
                request.address()
        );
    }

    @PreAuthorize("hasRole('CLIENT') and !hasRole('MANAGER')")
    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        List<Order> orders = orderService.getMyOrders(authentication.getName());
        if (orders.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("Нет бронирований"));
        }
        List<OrderResponse> response = orders.stream()
                .map(orderMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
