package com.konalyan.cleaning.cleaning_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class VerificationCodeAlreadyUsedException extends BusinessException {

    public VerificationCodeAlreadyUsedException() {
        super(
                "VERIFICATION_CODE_ALREADY_USED",
                "Код подтверждения уже был использован"
        );
    }
}
