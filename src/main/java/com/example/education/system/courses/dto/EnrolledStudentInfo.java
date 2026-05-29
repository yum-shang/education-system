package com.example.education.system.courses.dto;

import lombok.Data;

@Data
public class EnrolledStudentInfo {
    private Integer enrollmentId;
    private Integer studentId;
    private String studentName;
    private String studentNumber;
    private String major;
    private String grade;
    private String clazz;
    private String department;
    private String phone;
    private String status;
}
