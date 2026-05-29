package com.example.education.system.grades.dto;

import lombok.Data;

@Data
public class EnrolledStudentTemplateRow {
    private Integer enrollmentId;
    private String studentName;
    private String studentNumber;
    private Double score;
    private String gradeLevel;
    private String comment;
}
