package com.example.education.system.research.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProjectListResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private List<ProjectInfo> list;
        private Integer total;
        private Integer page;
        private Integer pageSize;
    }

    @lombok.Data
    public static class ProjectInfo {
        private Integer projectId;
        private String projectName;
        private String description;
        private Integer teacherId;
        private String teacherName;
        private String status;
        private String startDate;
        private String endDate;
        private String createdAt;
    }
}