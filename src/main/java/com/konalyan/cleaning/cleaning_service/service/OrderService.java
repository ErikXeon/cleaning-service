package com.konalyan.cleaning.cleaning_service.service;

import com.konalyan.cleaning.cleaning_service.entity.CleaningService;
import com.konalyan.cleaning.cleaning_service.entity.Order;
import com.konalyan.cleaning.cleaning_service.entity.User;
import com.konalyan.cleaning.cleaning_service.enums.OrderStatus;
import com.konalyan.cleaning.cleaning_service.exception.BadRequest;
import com.konalyan.cleaning.cleaning_service.exception.NotFoundException;
import com.konalyan.cleaning.cleaning_service.exception.UserNotFoundException;
import com.konalyan.cleaning.cleaning_service.repository.CleaningServiceRepository;
import com.konalyan.cleaning.cleaning_service.repository.LoginAttemptRepository;
import com.konalyan.cleaning.cleaning_service.repository.OrderRepository;
import com.konalyan.cleaning.cleaning_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CleaningServiceRepository cleaningServiceRepository;

    @Transactional
    public Order createOrder(
            String clientEmail,
            LocalDateTime dateTime,
            List<Long> serviceIds,
            String notes
    ){
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new UserNotFoundException(clientEmail));

        if(!isTimeSlotFree(dateTime)) {
            throw new BadRequest("the selected slot is already occupied");
        }

        List<CleaningService> services = cleaningServiceRepository.findAllById(serviceIds);
        if(services.isEmpty()){
            throw new BadRequest("drink at least one favor");
        }

        BigDecimal totalPrice = calculateTotalPrice(services);

        Order order = Order.builder()
                .client(client)
                .dateTime(dateTime)
                .services(services)
                .notes(notes)
                .status(OrderStatus.NEW)
                .totalPrice(totalPrice)
                .build();

        return orderRepository.save(order);
    }

    public List<Order> getMyOrders(String clientEmail) {
        return orderRepository.findAllByClientEmail(clientEmail);
    }

    public boolean isTimeSlotFree(LocalDateTime dateTime) {
        return orderRepository.countByDateTime(dateTime) == 0;
    }

    private BigDecimal calculateTotalPrice(List<CleaningService> services) {
        return services.stream()
                .map(CleaningService::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Order assignCleaner(Long orderId, String cleanerEmail, String managerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        User cleaner = userRepository.findByEmail(cleanerEmail)
                .orElseThrow(() -> new NotFoundException("Уборщик не найден"));

        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new NotFoundException("Менеджер не найден"));

        order.setCleaningStaff(cleaner);
        order.setManager(manager);
        order.setStatus(OrderStatus.IN_PROGRESS);

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status, String managerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new NotFoundException("Менеджер не найден"));

        order.setStatus(status);
        order.setManager(manager);

        return orderRepository.save(order);
    }

    public List<Order> getOrdersForManager() {
        return orderRepository.findAll();
    }

    public byte[] generatePdfForCleaner(String cleanerEmail, LocalDateTime date) {
        // TODO: интеграция с PDF-сервисом
        return new byte[0];
    }

}
