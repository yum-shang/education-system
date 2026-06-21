package com.example.education.system.research.model;

import lombok.Data;
import java.sql.Timestamp;
import java.util.Date;

@Data
public class ResearchProject {
    private Integer projectId;
    private String projectName;
    private String description;
    private Integer teacherId;
    private String status;
    private String tags;
    private Date startDate;
    private Date endDate;
    private Timestamp createdAt;
}