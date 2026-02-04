package com.konalyan.cleaning.cleaning_service.controller;

import com.konalyan.cleaning.cleaning_service.entity.Order;
import com.konalyan.cleaning.cleaning_service.enums.OrderStatus;
import com.konalyan.cleaning.cleaning_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerOrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/orders")
    public List<Order> getOrdersForManager() {
        return orderService.getOrdersForManager();
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/orders/{orderId}/assign")
    public Order assignCleaner(@PathVariable Long orderId,
                               @RequestParam String cleanerEmail,
                               @RequestParam String managerEmail) {
        return orderService.assignCleaner(orderId, cleanerEmail, managerEmail);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/orders/{orderId}/status")
    public Order updateOrderStatus(@PathVariable Long orderId,
                                   @RequestParam OrderStatus status,
                                   @RequestParam String managerEmail) {
        return orderService.updateOrderStatus(orderId, status, managerEmail);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/orders/pdf/{cleanerEmail}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable String cleanerEmail) {
        byte[] pdf = orderService.generatePdfForCleaner(cleanerEmail, LocalDateTime.now());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tasks.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}