package com.paymesh.userservice.service;

import com.paymesh.common.dto.auth.AuthResponse;
import com.paymesh.common.dto.auth.LoginRequest;
import com.paymesh.common.dto.auth.RegisterRequest;
import com.paymesh.common.exception.DuplicateRequestException;
import com.paymesh.common.exception.UnauthorizedException;
import com.paymesh.userservice.entity.User;
import com.paymesh.userservice.repository.UserRepository;
import com.paymesh.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                passwordEncoder,
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
                86400000L
        );
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "password123", "Alice Smith", "1234567890");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        User savedUser = new User("alice", "alice@example.com", "hashedPassword", "Alice Smith", "1234567890");
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("alice", response.getUsername());
        assertEquals(10L, response.getUserId());
    }

    @Test
    void testRegisterDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "password123", "Alice Smith", "1234567890");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(DuplicateRequestException.class, () -> userService.register(request));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest("alice", "password123");
        User user = new User("alice", "alice@example.com", "hashedPassword", "Alice Smith", "1234567890");
        user.setId(10L);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        AuthResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals(10L, response.getUserId());
    }

    @Test
    void testLoginInvalidPassword() {
        LoginRequest loginRequest = new LoginRequest("alice", "wrongpass");
        User user = new User("alice", "alice@example.com", "hashedPassword", "Alice Smith", "1234567890");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.login(loginRequest));
    }
}
