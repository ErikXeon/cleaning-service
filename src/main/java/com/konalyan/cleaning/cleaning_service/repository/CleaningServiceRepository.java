package com.konalyan.cleaning.cleaning_service.repository;

import com.konalyan.cleaning.cleaning_service.entity.CleaningService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CleaningServiceRepository extends JpaRepository<CleaningService, Long> {

}
