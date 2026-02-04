package com.konalyan.cleaning.cleaning_service.controller;

import com.konalyan.cleaning.cleaning_service.dto.UserResponse;
import com.konalyan.cleaning.cleaning_service.exception.UnauthorizedException;
import com.konalyan.cleaning.cleaning_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
@Slf4j
public class ProfileController {
    private final UserService userService;

    @GetMapping()
    public UserResponse getProfile(Authentication authentication) {
        log.info("AUTH NAME = {}", authentication.getName());
        return userService.getProfile(authentication.getName());
    }
}
