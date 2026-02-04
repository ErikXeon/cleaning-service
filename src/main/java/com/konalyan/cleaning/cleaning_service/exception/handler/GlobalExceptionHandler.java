package com.konalyan.cleaning.cleaning_service.exception.handler;

import com.konalyan.cleaning.cleaning_service.dto.error.ErrorResponse;
import com.konalyan.cleaning.cleaning_service.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case "TOO_MANY_LOGIN_ATTEMPTS", "VERIFICATION_RESEND_TOO_SOON" -> HttpStatus.TOO_MANY_REQUESTS;
            case "BAD_CREDENTIALS", "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "USER_NOT_FOUND", "NOTFOUND" -> HttpStatus.NOT_FOUND;
            case "VERIFICATION_SEND_FAILED" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Внутренняя ошибка сервера",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
