package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.request.LoginRequest;
import com.aryan.springboot.leavemanagement.request.RegisterRequest;
import com.aryan.springboot.leavemanagement.response.LoginResponse;
import com.aryan.springboot.leavemanagement.response.RegisterResponse;
import com.aryan.springboot.leavemanagement.response.UserResponse;

import java.util.List;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    LoginResponse login(LoginRequest request);
}