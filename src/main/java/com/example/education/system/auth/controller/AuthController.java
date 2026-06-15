package com.example.education.system.auth.controller;

import com.example.education.system.auth.dto.RegisterRequest;
import com.example.education.system.auth.dto.LoginRequest;
import com.example.education.system.auth.dto.AuthResponse;
import com.example.education.system.auth.dto.ForgotPasswordRequest;
import com.example.education.system.auth.dto.ResetPasswordRequest;
import com.example.education.system.auth.service.AuthService;
import com.example.education.system.auth.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<AuthResponse>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return passwordResetService.requestPasswordReset(request.getEmail())
                .map(message -> {
                    AuthResponse response = new AuthResponse();
                    response.setCode(200);
                    response.setMessage(message);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    AuthResponse response = new AuthResponse();
                    response.setCode(400);
                    response.setMessage(e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(response));
                });
    }

    @PostMapping("/reset-password")
    public Mono<ResponseEntity<AuthResponse>> resetPassword(@RequestBody ResetPasswordRequest request) {
        return passwordResetService.resetPassword(request.getToken(), request.getNewPassword())
                .map(message -> {
                    AuthResponse response = new AuthResponse();
                    response.setCode(200);
                    response.setMessage(message);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    AuthResponse response = new AuthResponse();
                    response.setCode(400);
                    response.setMessage(e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(response));
                });
    }
}