package com.example.education.system.grades.dto;

import lombok.Data;
import java.util.List;

@Data
public class GradeListResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private List<GradeInfo> list;
        private Integer total;
        private Integer page;
        private Integer pageSize;
    }

    @lombok.Data
    public static class GradeInfo {
        private Integer gradeId;
        private Integer studentId;
        private String studentName;
        private String studentNumber;
        private Double score;
        private String gradeLevel;
        private String comment;
        private String createdAt;
        private String courseName;
        private Double credit;
        private String teacherName;
        private String semester;
        private Integer year;
    }
}