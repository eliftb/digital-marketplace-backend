package com.pazaryeri.service;

import com.pazaryeri.dto.request.AuthRequest;
import com.pazaryeri.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(AuthRequest.Register request);
    AuthResponse login(AuthRequest.Login request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
    void forgotPassword(AuthRequest.ForgotPassword request);
    void resetPassword(AuthRequest.ResetPassword request);
    void changePassword(String email, AuthRequest.ChangePassword request);
}
