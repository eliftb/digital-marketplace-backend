package com.pazaryeri.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthRequest {

    @Data
    public static class Register {
        @NotBlank(message = "Ad zorunludur")
        @Size(min = 2, max = 100)
        private String firstName;

        @NotBlank(message = "Soyad zorunludur")
        @Size(min = 2, max = 100)
        private String lastName;

        @NotBlank(message = "E-posta zorunludur")
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        private String email;

        @NotBlank(message = "Şifre zorunludur")
        @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir")
        private String password;

        private String phone;
    }

    @Data
    public static class Login {
        @NotBlank(message = "E-posta zorunludur")
        @Email
        private String email;

        @NotBlank(message = "Şifre zorunludur")
        private String password;
    }

    @Data
    public static class ForgotPassword {
        @NotBlank @Email
        private String email;
    }

    @Data
    public static class ResetPassword {
        @NotBlank private String token;

        @NotBlank
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir")
        private String newPassword;
    }

    @Data
    public static class RefreshToken {
        @NotBlank
        private String refreshToken;
    }

    @Data
    public static class ChangePassword {
        @NotBlank private String currentPassword;

        @NotBlank
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")
        private String newPassword;
    }
}
