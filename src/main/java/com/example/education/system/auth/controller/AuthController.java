package com.example.education.system.auth.controller;

import com.example.education.system.auth.dto.RegisterRequest;
import com.example.education.system.auth.dto.LoginRequest;
import com.example.education.system.auth.dto.AuthResponse;
import com.example.education.system.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}