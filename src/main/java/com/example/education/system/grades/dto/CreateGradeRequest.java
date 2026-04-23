package com.example.education.system.grades.dto;

import lombok.Data;

@Data
public class CreateGradeRequest {
    private Integer enrollmentId;
    private Double score;
    private String gradeLevel;
    private String comment;
}