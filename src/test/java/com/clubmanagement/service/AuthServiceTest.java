package com.clubmanagement.service;

import com.clubmanagement.dto.auth.RegisterRequest;
import com.clubmanagement.entity.User;
import com.clubmanagement.enums.RoleName;
import com.clubmanagement.exception.BusinessRuleException;
import com.clubmanagement.repository.UserRepository;
import com.clubmanagement.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register succeeds with unique username and email")
    void register_success() {
        when(userRepository.existsByUsernameAndDeletedFalse("newuser")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedFalse("new@club.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        User savedUser = User.builder().id(1L).username("newuser")
                .email("new@club.com").role(RoleName.PLAYER).enabled(true).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@club.com");
        request.setPassword("password123");
        request.setRole(RoleName.PLAYER);

        var response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getRole()).isEqualTo(RoleName.PLAYER);
    }

    @Test
    @DisplayName("register throws when username already taken")
    void register_duplicateUsername_throws() {
        when(userRepository.existsByUsernameAndDeletedFalse("admin")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setEmail("other@club.com");
        request.setPassword("pass");
        request.setRole(RoleName.PLAYER);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    @DisplayName("register throws when email already registered")
    void register_duplicateEmail_throws() {
        when(userRepository.existsByUsernameAndDeletedFalse("unique")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedFalse("existing@club.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("unique");
        request.setEmail("existing@club.com");
        request.setPassword("pass");
        request.setRole(RoleName.PLAYER);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Email already registered");
    }
}
