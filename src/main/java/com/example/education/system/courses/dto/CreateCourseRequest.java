package com.example.education.system.courses.dto;

import lombok.Data;

@Data
public class CreateCourseRequest {
    private String courseName;
    private Double credit;
    private String courseCode;
    private String description;
}