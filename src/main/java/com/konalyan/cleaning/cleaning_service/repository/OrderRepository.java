package com.konalyan.cleaning.cleaning_service.repository;

import com.konalyan.cleaning.cleaning_service.enums.OrderStatus;
import com.konalyan.cleaning.cleaning_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findAllByClientEmail(String email);
    int countByDateTime(LocalDateTime dateTime);
    boolean existsByCleaningStaffEmailAndDateTimeAndStatusIn(String email, LocalDateTime dateTime, List<OrderStatus> statuses);
    List<Order> findAllByCleaningStaffEmailAndDateTimeBetween(String email, LocalDateTime start, LocalDateTime end);
}
