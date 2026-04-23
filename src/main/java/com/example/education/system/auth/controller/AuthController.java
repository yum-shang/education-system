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

    @GetMapping("/verification-code")
    public AuthResponse getVerificationCode(
            @RequestParam String type,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {
        // 实现验证码发送逻辑
        AuthResponse response = new AuthResponse();
        response.setCode(200);
        response.setMessage("验证码发送成功");
        return response;
    }

    @PostMapping("/reset-password")
    public AuthResponse resetPassword(
            @RequestParam String email,
            @RequestParam String verificationCode,
            @RequestParam String newPassword) {
        // 实现密码重置逻辑
        AuthResponse response = new AuthResponse();
        response.setCode(200);
        response.setMessage("密码重置成功");
        return response;
    }
}