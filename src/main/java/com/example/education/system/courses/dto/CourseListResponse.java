package com.example.education.system.courses.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseListResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private List<CourseInfo> list;
        private Integer total;
        private Integer page;
        private Integer pageSize;
    }

    @lombok.Data
    public static class CourseInfo {
        private Integer courseId;
        private String courseName;
        private Double credit;
        private String courseCode;
        private String description;
    }
}