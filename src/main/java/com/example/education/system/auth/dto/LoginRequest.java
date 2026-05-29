package com.example.education.system.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String phone;
    private String password;
}