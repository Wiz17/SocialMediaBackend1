package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UserDTO;

public interface AuthService{

    UserDTO signup(SignupRequest signupRequest);

    String login(LoginRequest loginRequest);

    String refreshToken(String refreshToken);
    
}