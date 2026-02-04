package com.konalyan.cleaning.cleaning_service.service;

import org.springframework.stereotype.Service;

@Service
public class CodeGeneratorService {
    public String generateSixDigitCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
