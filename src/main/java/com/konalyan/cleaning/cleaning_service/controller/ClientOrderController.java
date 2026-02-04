package com.konalyan.cleaning.cleaning_service.controller;

import com.konalyan.cleaning.cleaning_service.dto.CreateOrderRequest;
import com.konalyan.cleaning.cleaning_service.entity.Order;
import com.konalyan.cleaning.cleaning_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientOrderController {

    private final OrderService orderService;

    // ------------------- ЛК клиента -------------------

    @PreAuthorize("hasRole('ROLE_CLIENT') and !hasRole('ROLE_MANAGER')")
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

    @PreAuthorize("hasRole('ROLE_CLIENT') and !hasRole('ROLE_MANAGER')")
    @GetMapping("/orders")
    public List<Order> getMyOrders(Authentication authentication) {
        return orderService.getMyOrders(authentication.getName());
    }
}
