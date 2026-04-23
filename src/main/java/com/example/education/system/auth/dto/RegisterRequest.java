package com.example.education.system.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String role;
    private String name;
    private String studentNumber;
    private String major;
    private String grade;
    private String clazz;
    private String title;
    private String department;
}