package com.konalyan.cleaning.cleaning_service.controller;

import com.konalyan.cleaning.cleaning_service.dto.AssignRoleRequest;
import com.konalyan.cleaning.cleaning_service.dto.UserResponse;
import com.konalyan.cleaning.cleaning_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/users")
@RequiredArgsConstructor
public class ManagerUserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/roles")
    public UserResponse assignRole(@RequestBody AssignRoleRequest request) {
        return userService.assignRole(request);
    }
}