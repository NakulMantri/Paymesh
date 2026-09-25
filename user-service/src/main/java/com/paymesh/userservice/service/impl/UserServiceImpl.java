package com.paymesh.userservice.service.impl;

import com.paymesh.common.dto.auth.AuthResponse;
import com.paymesh.common.dto.auth.LoginRequest;
import com.paymesh.common.dto.auth.RegisterRequest;
import com.paymesh.common.dto.auth.UserDto;
import com.paymesh.common.exception.DuplicateRequestException;
import com.paymesh.common.exception.ResourceNotFoundException;
import com.paymesh.common.exception.UnauthorizedException;
import com.paymesh.common.util.JwtUtils;
import com.paymesh.userservice.entity.User;
import com.paymesh.userservice.repository.UserRepository;
import com.paymesh.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final long jwtExpirationMs;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${paymesh.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String jwtSecret,
                           @Value("${paymesh.jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtUtils = new JwtUtils(jwtSecret, jwtExpirationMs);
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateRequestException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = new User(
                request.getUsername().trim().toLowerCase(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getPhoneNumber()
        );

        User savedUser = userRepository.save(user);
        String token = jwtUtils.generateToken(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRoles()
        );

        return new AuthResponse(
                token,
                "Bearer",
                jwtExpirationMs / 1000,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRoles()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsername().trim().toLowerCase();
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username/email or password");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("User account is disabled");
        }

        String token = jwtUtils.generateToken(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );

        return new AuthResponse(
                token,
                "Bearer",
                jwtExpirationMs / 1000,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToDto(user);
    }

    @Override
    public UserDto updateUserProfile(Long id, String fullName, String phoneNumber) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }

        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRoles(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
