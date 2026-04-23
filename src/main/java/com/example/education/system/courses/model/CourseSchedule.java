package com.example.education.system.courses.model;

import lombok.Data;

@Data
public class CourseSchedule {
    private Integer scheduleId;
    private Integer courseId;
    private Integer teacherId;
    private String classroom;
    private Integer dayOfWeek;
    private String startTime;
    private String endTime;
    private String semester;
    private Integer year;
}