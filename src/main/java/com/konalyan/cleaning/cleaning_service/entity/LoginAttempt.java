package com.konalyan.cleaning.cleaning_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "attempts")
    private int attempts;

    @Column(name = "lastAttempt")
    private LocalDateTime lastAttempt;

    @Column(name = "blocked")
    private boolean blocked;

    @Column(name = "blockedUntil")
    private LocalDateTime blockedUntil;
}