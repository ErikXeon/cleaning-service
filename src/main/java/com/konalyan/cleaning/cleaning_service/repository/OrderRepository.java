package com.konalyan.cleaning.cleaning_service.repository;

import com.konalyan.cleaning.cleaning_service.enums.OrderStatus;
import com.konalyan.cleaning.cleaning_service.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    @EntityGraph(attributePaths = {"client", "cleaningStaff", "manager", "services"})
    List<Order> findAllByClientEmail(String email);
    @EntityGraph(attributePaths = {"client", "cleaningStaff", "manager", "services"})
    List<Order> findAll();
    @EntityGraph(attributePaths = {"client", "cleaningStaff", "manager", "services"})
    Optional<Order> findDetailedById(Long id);
    List<Order> findAllByCleaningStaffEmailAndDateTimeBetween(String email, LocalDateTime start, LocalDateTime end);
    List<Order> findAllByDateTimeBetweenAndStatusIn(LocalDateTime start, LocalDateTime end, List<OrderStatus> statuses);
    List<Order> findAllByCleaningStaffEmailAndStatusInAndDateTimeBetween(String email, List<OrderStatus> statuses, LocalDateTime start, LocalDateTime end);
    boolean existsByCleaningStaffEmailAndDateTimeAndStatusIn(String email, LocalDateTime dateTime, List<OrderStatus> statuses);
}
