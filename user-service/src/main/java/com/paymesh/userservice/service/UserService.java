package com.paymesh.userservice.service;

import com.paymesh.common.dto.auth.AuthResponse;
import com.paymesh.common.dto.auth.LoginRequest;
import com.paymesh.common.dto.auth.RegisterRequest;
import com.paymesh.common.dto.auth.UserDto;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserDto getUserById(Long id);
    UserDto getUserByUsername(String username);
    UserDto updateUserProfile(Long id, String fullName, String phoneNumber);
}
