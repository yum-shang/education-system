package com.example.education.system.reports.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReportListResponse {
    private List<ReportInfo> list;
    private Long total;
    private Integer page;
    private Integer pageSize;

    @Data
    public static class ReportInfo {
        private Integer reportId;
        private String reportName;
        private String reportType;
        private String generatedAt;
    }
}
