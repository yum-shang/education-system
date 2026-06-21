package com.example.education.system.users.model;

import lombok.Data;

@Data
public class Teacher {
    private Integer teacherId;
    private Integer userId;
    private String name;
    private String title;
    private String department;
    private String bio;
}