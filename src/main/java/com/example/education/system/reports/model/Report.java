package com.example.education.system.reports.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class Report {
    private Integer reportId;
    private String reportName;
    private String reportType;
    private String content;
    private Integer generatedBy;
    private Timestamp generatedAt;
    private String filePath;
}
