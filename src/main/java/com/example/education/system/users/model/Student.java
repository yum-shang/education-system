package com.example.education.system.users.model;

import lombok.Data;

@Data
public class Student {
    private Integer studentId;
    private Integer userId;
    private String name;
    private String studentNumber;
    private String major;
    private String grade;
    private String clazz;
    private String department;
    private String gender;
}