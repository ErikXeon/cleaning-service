package com.konalyan.cleaning.cleaning_service.repository;

import com.konalyan.cleaning.cleaning_service.entity.User;
import com.konalyan.cleaning.cleaning_service.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByUserAndUsedFalse(User user);
    Optional<VerificationCode> findTopByEmailOrderBySentAtDesc(String email);
    void deleteByExpiresAtBefore(LocalDateTime time);
}
