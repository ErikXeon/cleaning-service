package com.konalyan.cleaning.cleaning_service.controller;

import com.konalyan.cleaning.cleaning_service.dto.*;
import com.konalyan.cleaning.cleaning_service.entity.User;
import com.konalyan.cleaning.cleaning_service.exception.ForbiddenException;
import com.konalyan.cleaning.cleaning_service.exception.TooManyLoginAttemptsException;
import com.konalyan.cleaning.cleaning_service.exception.BadCredentialsException;
import com.konalyan.cleaning.cleaning_service.exception.UserNotFoundException;
import com.konalyan.cleaning.cleaning_service.repository.UserRepository;
import com.konalyan.cleaning.cleaning_service.service.LoginAttemptService;
import com.konalyan.cleaning.cleaning_service.service.UserService;
import com.konalyan.cleaning.cleaning_service.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final VerificationService verificationService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;
    private final UserRepository userRepository;

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public String verifyUser(
            @Valid @RequestBody VerifyCodeRequest request
    ){
        User user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new UserNotFoundException(request.email()));
        if(user.getEnabled().equals(true)) {
            return "Пользователь " + user.getEmail() + " уже активирован";
        }
        verificationService.verify(request.email(), request.code());
        log.info("Метод verifyUser вызван");
        return "Пользователь " + request.email() + " успешно подтверждён";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null &&
                auth.isAuthenticated() &&
                !(auth instanceof AnonymousAuthenticationToken)) {

            return new LoginResponse(
                    "Вы уже вошли в систему",
                    userService.getProfile(auth.getName())
            );
        }

        String email = request.email();

        if (loginAttemptService.isBlocked(email)) {
            throw new TooManyLoginAttemptsException();
        }

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, request.password());

            Authentication authentication = authenticationManager.authenticate(authToken);

            loginAttemptService.loginSucceeded(email);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            httpRequest.getSession(true)
                    .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                            SecurityContextHolder.getContext());

            return new  LoginResponse(
                    "Вы успешно вошли в систему",
                    userService.getProfile(authentication.getName())
            );

        } catch (Exception e) {
            loginAttemptService.loginFailed(email);
            log.warn("Неуспешная попытка входа для {}: {}", email, e.getMessage());
            throw new BadCredentialsException();        }
    }

    @PostMapping("/register")
    public UserResponse register(
            @RequestBody CreateUserRequest request
    ){
        log.info("Метод register вызван");
        return userService.registerUser(request);
    }

    @PostMapping("/resend-code")
    public MessageResponse resendCode(
            @RequestBody ResendCodeRequest request,
            Authentication authentication
    ) {
        if (authentication != null && authentication.isAuthenticated()) {
            throw new ForbiddenException();
        }

        verificationService.resendCode(request.email());
        return new MessageResponse("Код отправлен повторно");
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return new MessageResponse("Вы вышли из системы");
    }

}
