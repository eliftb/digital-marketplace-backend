package com.pazaryeri.service.impl;

import com.pazaryeri.dto.response.AuthResponse;
import com.pazaryeri.dto.response.DashboardResponse;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.entity.PlatformSetting;
import com.pazaryeri.entity.User;
import com.pazaryeri.enums.AccountStatus;
import com.pazaryeri.enums.OrderStatus;
import com.pazaryeri.enums.UserRole;
import com.pazaryeri.exception.BusinessException;
import com.pazaryeri.exception.ResourceNotFoundException;
import com.pazaryeri.repository.*;
import com.pazaryeri.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProducerProfileRepository producerProfileRepository;
    private final CommissionTransactionRepository commissionTransactionRepository;
    private final PlatformSettingRepository platformSettingRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        BigDecimal monthlyRevenue = orderRepository.calculateRevenueByDateRange(monthStart, now);
        BigDecimal monthlyCommission = commissionTransactionRepository.sumCommissionByDateRange(monthStart, now);

        return DashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalProducers(userRepository.countByRole(UserRole.PRODUCER))
                .totalConsumers(userRepository.countByRole(UserRole.CONSUMER))
                .pendingProducerApprovals(producerProfileRepository.countByApprovalStatus(AccountStatus.PENDING_APPROVAL))
                .totalProducts(productRepository.count())
                .activeProducts(productRepository.countByActiveTrue())
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus(OrderStatus.PENDING))
                .deliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .totalRevenue(totalRevenue)
                .totalCommissionEarned(commissionTransactionRepository.sumCommissionByDateRange(
                        LocalDateTime.of(2000, 1, 1, 0, 0), now))
                .monthlyRevenue(monthlyRevenue)
                .monthlyCommission(monthlyCommission)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuthResponse.UserDto> getAllUsers(UserRole role, AccountStatus status,
                                                          String search, Pageable pageable) {
        Page<User> page = userRepository.searchUsers(search, role, status, pageable);
        return PageResponse.of(page.map(this::toUserDto));
    }

    @Override
    @Transactional
    public void banUser(Long userId, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", userId));

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("Süper admin banlanamaz");
        }

        user.setStatus(AccountStatus.BANNED);
        userRepository.save(user);
        log.info("Kullanıcı banlandı: {} - Admin: {}", user.getEmail(), adminEmail);
    }

    @Override
    @Transactional
    public void activateUser(Long userId, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", userId));

        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("Kullanıcı aktifleştirildi: {} - Admin: {}", user.getEmail(), adminEmail);
    }

    @Override
    @Transactional
    public void updatePlatformSetting(String key, String value, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin bulunamadı"));

        PlatformSetting setting = platformSettingRepository.findByKey(key)
                .orElse(PlatformSetting.builder().key(key).build());

        setting.setValue(value);
        setting.setUpdatedBy(admin);
        platformSettingRepository.save(setting);

        log.info("Platform ayarı güncellendi: {} = {} - Admin: {}", key, value, adminEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public String getPlatformSetting(String key) {
        return platformSettingRepository.findByKey(key)
                .map(PlatformSetting::getValue)
                .orElseThrow(() -> new ResourceNotFoundException("Platform ayarı bulunamadı: " + key));
    }

    private AuthResponse.UserDto toUserDto(User user) {
        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
