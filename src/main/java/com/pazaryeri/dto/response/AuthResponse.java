package com.pazaryeri.dto.response;

import com.pazaryeri.enums.UserRole;
import com.pazaryeri.enums.AccountStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserDto user;

    @Data @Builder
    public static class UserDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private UserRole role;
        private AccountStatus status;
        private LocalDateTime createdAt;
    }
}
