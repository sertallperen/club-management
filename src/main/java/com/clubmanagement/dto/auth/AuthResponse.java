package com.clubmanagement.dto.auth;

import com.clubmanagement.enums.RoleName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private RoleName role;
    private Long userId;
}
