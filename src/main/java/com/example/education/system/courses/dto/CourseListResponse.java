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
        private List<?> list;
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

    @lombok.Data
    public static class ScheduleInfo {
        private Integer scheduleId;
        private Integer courseId;
        private String courseName;
        private String courseCode;
        private Integer teacherId;
        private String teacherName;
        private String classroom;
        private Integer dayOfWeek;
        private String startTime;
        private String endTime;
        private String semester;
        private Integer year;
    }
}