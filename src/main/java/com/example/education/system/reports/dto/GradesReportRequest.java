package com.example.education.system.reports.dto;

import lombok.Data;

@Data
public class GradesReportRequest {
    private String semester;
    private Integer year;
    private Integer courseId;
}
