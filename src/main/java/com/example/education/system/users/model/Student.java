package com.example.education.system.users.model;

import lombok.Data;

@Data
public class Student {
    private Integer studentId;
    private String name;
    private String studentNumber;
    private String major;
    private String grade;
    private String clazz;
}