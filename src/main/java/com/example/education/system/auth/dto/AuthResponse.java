package com.example.education.system.auth.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private Integer userId;
        private String username;
        private String role;
        private String token;
        private UserInfo user;
        private Boolean exists;
    }

    @lombok.Data
    public static class UserInfo {
        private Integer userId;
        private String username;
        private String role;
        private Integer avatarId;
    }
}