package com.example.education.system.auth.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class User {
    private Integer userId;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String role;
    private Integer avatarId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}