package com.example.education.system.users.dto;

import lombok.Data;
import java.util.List;

@Data
public class StudentEnrollmentResponse {
    private Integer code;
    private String message;
    private List<EnrollmentInfo> data;

    @lombok.Data
    public static class EnrollmentInfo {
        private Integer enrollmentId;
        private Integer scheduleId;
        private String courseName;
        private String courseCode;
        private Double credit;
        private String teacherName;
        private String teacherTitle;
        private String teacherDept;
        private String teacherEmail;
        private String classroom;
        private Integer dayOfWeek;
        private String startTime;
        private String endTime;
        private String semester;
        private Integer year;
        private Double score;
        private String gradeLevel;
        private String status;
    }
}
