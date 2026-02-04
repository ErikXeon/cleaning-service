package com.konalyan.cleaning.cleaning_service.service;

import com.konalyan.cleaning.cleaning_service.dto.EmailNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, EmailNotification> kafkaTemplate;

    private static final String TOPIC = "user-verification";

    public void sendVerificationCode(EmailNotification notification) {
        kafkaTemplate.send(TOPIC, notification);
    }
}
