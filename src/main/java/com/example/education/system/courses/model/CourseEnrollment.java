package com.example.education.system.courses.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseEnrollment {
    private Integer enrollmentId;
    private Integer studentId;
    private Integer scheduleId;
    private LocalDateTime enrollTime;
    private String status;
}
