package com.konalyan.cleaning.cleaning_service.service;

import com.konalyan.cleaning.cleaning_service.dto.CreateUserRequest;
import com.konalyan.cleaning.cleaning_service.dto.EmailNotification;
import com.konalyan.cleaning.cleaning_service.dto.UserResponse;
import com.konalyan.cleaning.cleaning_service.entity.Role;
import com.konalyan.cleaning.cleaning_service.entity.User;
import com.konalyan.cleaning.cleaning_service.entity.VerificationCode;
import com.konalyan.cleaning.cleaning_service.exception.*;
import com.konalyan.cleaning.cleaning_service.mapper.UserMapper;
import com.konalyan.cleaning.cleaning_service.repository.RoleRepository;
import com.konalyan.cleaning.cleaning_service.repository.UserRepository;
import com.konalyan.cleaning.cleaning_service.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.konalyan.cleaning.cleaning_service.dto.AssignRoleRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;
    private final CodeGeneratorService codeGeneratorService;

    public UserResponse registerUser(CreateUserRequest request) {

        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new UserAlreadyExistsException(request.email());
        });

        User user = userMapper.toEntity(request);
        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(null);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(false);
        user.setRoles(Set.of(clientRole));

        String generatedCode = codeGeneratorService.generateSixDigitCode();

        VerificationCode lastCode = verificationCodeRepository
                .findTopByEmailOrderBySentAtDesc(user.getEmail())
                .orElse(null);

        int resendDelaySec = 180;
        if (lastCode != null) {
            long secondsSinceLast = Duration.between(lastCode.getSentAt(), LocalDateTime.now()).getSeconds();
            if (secondsSinceLast < resendDelaySec) {
                long remaining = resendDelaySec - secondsSinceLast;
                throw new RuntimeException("Код уже отправлен, попробуйте через " + remaining + " секунд");
            }
        }

        try {
            EmailNotification notification = new EmailNotification(user.getEmail(), generatedCode);
            kafkaProducerService.sendVerificationCode(notification);
            log.info("Sent verification code event to Kafka for {}", user.getEmail());
        } catch (Exception e) {
            log.error("Не удалось отправить код пользователю {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Не удалось отправить код подтверждения, попробуйте позже");
        }

        userRepository.save(user);

        VerificationCode code = new VerificationCode();
        code.setUser(user);
        code.setCode(generatedCode);
        code.setAttempts(0);
        code.setUsed(false);
        code.setExpiresAt(LocalDateTime.now().plusMinutes(3));
        code.setSentAt(LocalDateTime.now());
        code.setEmail(user.getEmail());

        verificationCodeRepository.save(code);

        return userMapper.toUserResponse(user);
    }

    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (!user.getEnabled()) {
            throw new UserNotActiveException();
        }

        return userMapper.toUserResponse(user);

    }

    public UserResponse assignRole(AssignRoleRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        Set<String> allowedRoles = Set.of("ROLE_CLIENT", "ROLE_MANAGER", "ROLE_CLEANER");
        if (!allowedRoles.contains(request.role())) {
            throw new BadRequest("Некорректная роль для назначения");
        }

        Role targetRole = roleRepository.findByName(request.role())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(Set.of(targetRole));

        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }
}
