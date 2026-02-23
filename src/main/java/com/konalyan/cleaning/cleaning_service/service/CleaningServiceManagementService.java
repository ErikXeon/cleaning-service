package com.konalyan.cleaning.cleaning_service.service;

import com.konalyan.cleaning.cleaning_service.dto.CleaningServiceResponse;
import com.konalyan.cleaning.cleaning_service.entity.CleaningService;
import com.konalyan.cleaning.cleaning_service.exception.BadRequest;
import com.konalyan.cleaning.cleaning_service.exception.NotFoundException;
import com.konalyan.cleaning.cleaning_service.repository.CleaningServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CleaningServiceManagementService {

    private final CleaningServiceRepository cleaningServiceRepository;

    @Transactional(readOnly = true)
    public List<CleaningServiceResponse> getAllServices() {
        return cleaningServiceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CleaningServiceResponse updateServicePrice(Long serviceId, BigDecimal price) {
        if (price == null) {
            throw new BadRequest("Укажите новую цену услуги");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequest("Цена услуги должна быть больше нуля");
        }

        CleaningService service = cleaningServiceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));

        service.setPrice(price);
        return toResponse(cleaningServiceRepository.save(service));
    }

    private CleaningServiceResponse toResponse(CleaningService service) {
        return new CleaningServiceResponse(
                service.getId(),
                service.getName(),
                service.getPrice(),
                service.getDurationMinutes()
        );
    }
}