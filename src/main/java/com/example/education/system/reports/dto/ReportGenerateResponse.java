package com.example.education.system.reports.dto;

import lombok.Data;

@Data
public class ReportGenerateResponse {
    private Integer reportId;
    private String reportName;
    private String reportType;
    private String filePath;
}
