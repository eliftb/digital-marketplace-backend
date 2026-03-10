package com.pazaryeri.controller;

import com.pazaryeri.dto.request.AuthRequest;
import com.pazaryeri.dto.response.ApiResponse;
import com.pazaryeri.dto.response.AuthResponse;
import com.pazaryeri.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Kimlik Doğrulama", description = "Kayıt, giriş ve şifre yönetimi")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Yeni kullanıcı kaydı")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody AuthRequest.Register request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kayıt başarılı", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Kullanıcı girişi")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest.Login request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Giriş başarılı", response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Access token yenileme")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody AuthRequest.RefreshToken request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Çıkış")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody AuthRequest.RefreshToken request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.successMessage("Çıkış başarılı"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Şifre sıfırlama e-postası gönder")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody AuthRequest.ForgotPassword request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.successMessage("Islem basarili"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Şifre sıfırla")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody AuthRequest.ResetPassword request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.successMessage("Şifreniz başarıyla sıfırlandı"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Şifre değiştir")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AuthRequest.ChangePassword request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.successMessage("Şifreniz başarıyla değiştirildi"));
    }
}
