package com.konalyan.cleaning.cleaning_service.controller;

import com.konalyan.cleaning.cleaning_service.dto.AssignCleanerRequest;
import com.konalyan.cleaning.cleaning_service.dto.MessageResponse;
import com.konalyan.cleaning.cleaning_service.dto.OrderResponse;
import com.konalyan.cleaning.cleaning_service.dto.UpdateOrderStatusRequest;
import com.konalyan.cleaning.cleaning_service.entity.Order;
import com.konalyan.cleaning.cleaning_service.enums.OrderStatus;
import com.konalyan.cleaning.cleaning_service.exception.BadRequest;
import com.konalyan.cleaning.cleaning_service.mapper.OrderMapper;
import com.konalyan.cleaning.cleaning_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerOrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/orders")
    public ResponseEntity<?> getOrdersForManager() {
        List<Order> orders = orderService.getOrdersForManager();
        if (orders.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("Нет бронирований"));
        }
        List<OrderResponse> response = orders.stream()
                .map(orderMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/orders/{orderId}/assign")
    public OrderResponse assignCleaner(@PathVariable Long orderId,
                                       @RequestBody AssignCleanerRequest request,
                                       org.springframework.security.core.Authentication authentication) {
        Order order = orderService.assignCleaner(orderId, request.cleanerEmail(), authentication.getName());
        return orderMapper.toResponse(order);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/orders/{orderId}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {

        if (request.status() == null) {
            throw new BadRequest("Статус заказа не указан или некорректен");
        }

        Order order = orderService.updateOrderStatus(orderId, request.status(), authentication.getName());
        return orderMapper.toResponse(order);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/orders/pdf/{cleanerEmail}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable String cleanerEmail,
                                              @RequestParam(required = false) LocalDate date) {
        byte[] pdf = orderService.generatePdfForCleaner(cleanerEmail, date);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tasks.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}