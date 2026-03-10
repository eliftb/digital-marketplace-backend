package com.pazaryeri;

import com.pazaryeri.dto.request.ProducerProfileRequest;
import com.pazaryeri.dto.response.ProducerProfileResponse;
import com.pazaryeri.entity.ProducerProfile;
import com.pazaryeri.entity.User;
import com.pazaryeri.enums.AccountStatus;
import com.pazaryeri.enums.UserRole;
import com.pazaryeri.exception.BusinessException;
import com.pazaryeri.repository.*;
import com.pazaryeri.service.EmailService;
import com.pazaryeri.service.impl.ProducerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProducerServiceTest {

    @Mock private ProducerProfileRepository producerProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private CityRepository cityRepository;
    @Mock private DistrictRepository districtRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private ProducerServiceImpl producerService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).email("user@test.com")
                .firstName("Mehmet").lastName("Test")
                .role(UserRole.CONSUMER).build();
    }

    @Test
    void registerAsProducer_whenNoExistingProfile_shouldSucceed() {
        ProducerProfileRequest request = new ProducerProfileRequest();
        request.setStoreName("Mehmet'in Çiftliği");
        request.setStoreDescription("Taze organik ürünler");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(producerProfileRepository.existsByUserId(1L)).thenReturn(false);
        when(producerProfileRepository.save(any())).thenAnswer(inv -> {
            ProducerProfile pp = inv.getArgument(0);
            return ProducerProfile.builder()
                    .id(1L).user(user).storeName(pp.getStoreName())
                    .approvalStatus(AccountStatus.PENDING_APPROVAL)
                    .commissionRate(new BigDecimal("10.00"))
                    .totalSales(BigDecimal.ZERO).ratingCount(0)
                    .build();
        });
        when(userRepository.save(any())).thenReturn(user);

        ProducerProfileResponse response = producerService.registerAsProducer("user@test.com", request);

        assertThat(response.getStoreName()).isEqualTo("Mehmet'in Çiftliği");
        assertThat(response.getApprovalStatus()).isEqualTo(AccountStatus.PENDING_APPROVAL);
        verify(userRepository).save(any()); // Rol PRODUCER yapıldı mı?
    }

    @Test
    void registerAsProducer_whenProfileExists_shouldThrow() {
        ProducerProfileRequest request = new ProducerProfileRequest();
        request.setStoreName("Tekrar Mağaza");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(producerProfileRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> producerService.registerAsProducer("user@test.com", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten");
    }

    @Test
    void approveProducer_shouldSetActiveAndSendEmail() {
        User admin = User.builder().id(99L).email("admin@test.com").role(UserRole.ADMIN).build();
        ProducerProfile profile = ProducerProfile.builder()
                .id(1L).user(user).storeName("Onay Bekleyen Mağaza")
                .approvalStatus(AccountStatus.PENDING_APPROVAL)
                .commissionRate(new BigDecimal("10.00"))
                .totalSales(BigDecimal.ZERO).ratingCount(0)
                .build();

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(producerProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(producerProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).sendProducerApprovalEmail(anyString(), anyString());

        ProducerProfileResponse response = producerService.approveProducer(1L, "admin@test.com");

        assertThat(response.getApprovalStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(emailService).sendProducerApprovalEmail(anyString(), anyString());
    }

    @Test
    void updateCommissionRate_outOfRange_shouldThrow() {
        assertThatThrownBy(() -> producerService.updateCommissionRate(1L, new BigDecimal("150"), "admin@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("0-100");
    }
}
