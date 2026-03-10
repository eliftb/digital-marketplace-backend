package com.pazaryeri.service;

import com.pazaryeri.dto.request.ProducerProfileRequest;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.ProducerProfileResponse;
import com.pazaryeri.enums.AccountStatus;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProducerService {
    ProducerProfileResponse registerAsProducer(String userEmail, ProducerProfileRequest request);
    ProducerProfileResponse updateProfile(String producerEmail, ProducerProfileRequest request);
    ProducerProfileResponse getMyProfile(String producerEmail);
    ProducerProfileResponse getProducerById(Long id);
    PageResponse<ProducerProfileResponse> searchProducers(String search, Long cityId, AccountStatus status, Pageable pageable);
    ProducerProfileResponse approveProducer(Long producerProfileId, String adminEmail);
    ProducerProfileResponse rejectProducer(Long producerProfileId, String reason, String adminEmail);
    void updateCommissionRate(Long producerProfileId, BigDecimal rate, String adminEmail);
}
