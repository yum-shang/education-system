package com.example.education.system.research.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class ApplicationWithStudent {
    private Integer applicationId;
    private Integer projectId;
    private String projectName;
    private Integer studentId;
    private String studentName;
    private String studentNumber;
    private String major;
    private String grade;
    private String clazz;
    private String applicationLetter;
    private String status;
    private Timestamp applyTime;
    private Timestamp reviewTime;
}