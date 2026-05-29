package com.example.education.system.grades.dto;

import lombok.Data;

@Data
public class StudentGradeReportRow {
    private String courseName;
    private String courseCode;
    private Double credit;
    private Double score;
    private String gradeLevel;
    private String comment;
}
