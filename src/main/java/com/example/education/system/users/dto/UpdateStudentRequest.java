package com.example.education.system.users.dto;

import lombok.Data;

@Data
public class UpdateStudentRequest {
    private String name;
    private String email;
    private String phone;
    private String studentNumber;
    private String major;
    private String grade;
    private String clazz;
    private String department;
    private String gender;
}
