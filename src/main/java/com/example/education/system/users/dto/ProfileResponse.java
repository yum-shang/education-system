package com.example.education.system.users.dto;

import lombok.Data;

@Data
public class ProfileResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private Integer userId;
        private String username;
        private String email;
        private String phone;
        private String role;
        private Integer avatarId;
        private String name;
        private String studentNumber;
        private String major;
        private String grade;
        private String clazz;
        private String department;
        private String gender;
        private String title;
        private String bio;
        private Double totalCredit;
    }
}