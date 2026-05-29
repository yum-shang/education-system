package com.example.education.system.courses.dto;

import lombok.Data;

@Data
public class EnrollmentInfo {
    private Integer enrollmentId;
    private Integer scheduleId;
    private String status;
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
}
