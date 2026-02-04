package com.konalyan.cleaning.cleaning_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konalyan.cleaning.cleaning_service.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                401,
                "UNAUTHORIZED",
                "Вы не вошли в аккаунт",
                request.getRequestURI()
        );

        // используем objectMapper из Spring
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
