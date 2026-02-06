package com.konalyan.cleaning.cleaning_service.controller;

import com.konalyan.cleaning.cleaning_service.dto.AssignCleanerRequest;
import com.konalyan.cleaning.cleaning_service.dto.UpdateOrderStatusRequest;
import com.konalyan.cleaning.cleaning_service.entity.Order;
import com.konalyan.cleaning.cleaning_service.enums.OrderStatus;
import com.konalyan.cleaning.cleaning_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerOrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/orders")
    public List<Order> getOrdersForManager() {
        return orderService.getOrdersForManager();
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/orders/{orderId}/assign")
    public Order assignCleaner(@PathVariable Long orderId,
                               @RequestBody AssignCleanerRequest request,
                               org.springframework.security.core.Authentication authentication) {
        return orderService.assignCleaner(orderId, request.cleanerEmail(), authentication.getName());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/orders/{orderId}/status")
    public Order updateOrderStatus(@PathVariable Long orderId,
                                   @RequestBody UpdateOrderStatusRequest request,
                                   org.springframework.security.core.Authentication authentication) {
        return orderService.updateOrderStatus(orderId, request.status(), authentication.getName());
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