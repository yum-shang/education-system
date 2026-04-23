package com.example.education.system.research.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class ProjectApplication {
    private Integer applicationId;
    private Integer projectId;
    private Integer studentId;
    private String applicationLetter;
    private String status;
    private Timestamp applyTime;
    private Timestamp reviewTime;
}