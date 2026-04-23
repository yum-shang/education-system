package com.example.education.system.grades.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class Grade {
    private Integer gradeId;
    private Integer enrollmentId;
    private Double score;
    private String gradeLevel;
    private Integer teacherId;
    private String comment;
    private Timestamp createdAt;
}