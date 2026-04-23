package com.example.education.system.courses.model;

import lombok.Data;

@Data
public class Course {
    private Integer courseId;
    private String courseName;
    private Double credit;
    private String courseCode;
    private String description;
}