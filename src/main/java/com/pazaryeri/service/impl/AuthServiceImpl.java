package com.pazaryeri.service.impl;

import com.pazaryeri.dto.request.AuthRequest;
import com.pazaryeri.dto.response.AuthResponse;
import com.pazaryeri.entity.PasswordResetToken;
import com.pazaryeri.entity.RefreshToken;
import com.pazaryeri.entity.User;
import com.pazaryeri.exception.BusinessException;
import com.pazaryeri.exception.ResourceNotFoundException;
import com.pazaryeri.repository.PasswordResetTokenRepository;
import com.pazaryeri.repository.RefreshTokenRepository;
import com.pazaryeri.repository.UserRepository;
import com.pazaryeri.security.JwtService;
import com.pazaryeri.security.UserDetailsServiceImpl;
import com.pazaryeri.service.AuthService;
import com.pazaryeri.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(AuthRequest.Register request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Bu e-posta adresi zaten kullanılıyor: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .build();

        userRepository.save(user);
        log.info("Yeni kullanıcı kaydedildi: {}", user.getEmail());

        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(AuthRequest.Login request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Eski refresh tokenları iptal et
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenStr)
                .orElseThrow(() -> new BusinessException("Geçersiz veya süresi dolmuş refresh token"));

        if (refreshToken.isExpired()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new BusinessException("Refresh token süresi dolmuş, lütfen tekrar giriş yapın");
        }

        User user = refreshToken.getUser();
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenStr)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional
    public void forgotPassword(AuthRequest.ForgotPassword request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            // Eski tokenları geçersiz kıl
            passwordResetTokenRepository.invalidateAllUserTokens(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusHours(2))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
            log.info("Şifre sıfırlama maili gönderildi: {}", user.getEmail());
        });
    }

    @Override
    @Transactional
    public void resetPassword(AuthRequest.ResetPassword request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BusinessException("Geçersiz veya kullanılmış şifre sıfırlama tokeni"));

        if (resetToken.isExpired()) {
            throw new BusinessException("Şifre sıfırlama tokeni süresi dolmuş");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Tüm aktif oturumları sonlandır
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        log.info("Şifre sıfırlandı: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void changePassword(String email, AuthRequest.ChangePassword request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Mevcut şifre hatalı");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllUserTokens(user.getId());
    }

    private AuthResponse generateAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());

        String accessToken = jwtService.generateToken(claims, userDetails);
        String refreshTokenStr = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();
    }
}
