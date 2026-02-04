package com.konalyan.cleaning.cleaning_service.repository;

import com.konalyan.cleaning.cleaning_service.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    Optional<LoginAttempt> findByEmail(String email);
    void deleteByLastAttemptBefore(LocalDateTime time);
}
