package com.example.education.system.innovation.dto;

import lombok.Data;
import java.util.List;

@Data
public class ApplicationListResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private List<ApplicationInfo> list;
        private Integer total;
        private Integer page;
        private Integer pageSize;
    }

    @lombok.Data
    public static class ApplicationInfo {
        private Integer applicationId;
        private Integer teamId;
        private String teamName;
        private Integer studentId;
        private String studentName;
        private String studentNumber;
        private String applicationLetter;
        private String status;
        private String applyTime;
        private String reviewTime;
    }
}
