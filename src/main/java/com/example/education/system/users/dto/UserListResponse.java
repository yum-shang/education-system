package com.example.education.system.users.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserListResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private List<UserInfo> list;
        private Integer total;
        private Integer page;
        private Integer pageSize;
    }

    @lombok.Data
    public static class UserInfo {
        private Integer userId;
        private String username;
        private String email;
        private String phone;
        private String role;
        private Integer avatarId;
        private String createdAt;
        private String name;
        private String studentNumber;
        private String major;
        private String grade;
        private String clazz;
        private String department;
        private String gender;
        private String title;
        private String bio;
    }
}