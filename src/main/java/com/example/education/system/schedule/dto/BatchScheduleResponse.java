package com.example.education.system.schedule.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchScheduleResponse {
    private Integer code;
    private String message;
    private List<ScheduleItemResult> data;

    @Data
    public static class ScheduleItemResult {
        private Integer scheduleId;
        private Integer courseId;
        private String courseName;
        private Integer teacherId;
        private String teacherName;
        private String classroomName;
        private Integer dayOfWeek;
        private Integer timeSlot;
        private String startTime;
        private String endTime;
        private String semester;
        private Integer year;
        private Boolean success;
        private String errorMessage;
    }
}
