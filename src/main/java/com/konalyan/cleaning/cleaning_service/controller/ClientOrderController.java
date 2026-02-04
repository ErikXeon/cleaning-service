package com.konalyan.cleaning.cleaning_service.controller;

import com.konalyan.cleaning.cleaning_service.entity.Order;
import com.konalyan.cleaning.cleaning_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientOrderController {

    private final OrderService orderService;

    // ------------------- ЛК клиента -------------------

    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @PostMapping("/orders")
    public Order createOrder(@RequestParam LocalDateTime dateTime,
                             @RequestParam List<Long> serviceIds,
                             @RequestParam(required = false) String notes,
                             @RequestParam(required = false) String address,
                             @RequestParam String clientEmail) {
        return orderService.createOrder(clientEmail, dateTime, serviceIds, notes, address);
    }

    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @GetMapping("/orders")
    public List<Order> getMyOrders(@RequestParam String clientEmail) {
        return orderService.getMyOrders(clientEmail);
    }
}
