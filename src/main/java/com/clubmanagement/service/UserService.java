package com.clubmanagement.service;

import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.user.UserRequest;
import com.clubmanagement.dto.user.UserResponse;
import com.clubmanagement.entity.User;
import com.clubmanagement.exception.BusinessRuleException;
import com.clubmanagement.exception.ResourceNotFoundException;
import com.clubmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PagedResponse<UserResponse> getAll(Pageable pageable) {
        return PagedResponse.of(userRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public UserResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new BusinessRuleException("Username already taken");
        }
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new BusinessRuleException("Email already registered");
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = findActive(id);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setRole(request.getRole());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void softDelete(Long id) {
        User user = findActive(id);
        user.setDeleted(true);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse restore(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setDeleted(false);
        user.setEnabled(true);
        return toResponse(userRepository.save(user));
    }

    private User findActive(Long id) {
        return userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
