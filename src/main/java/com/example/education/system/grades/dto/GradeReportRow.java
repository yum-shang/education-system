package com.example.education.system.grades.dto;

import lombok.Data;

@Data
public class GradeReportRow {
    private String studentName;
    private String studentNumber;
    private Double score;
    private String gradeLevel;
    private String comment;
}
