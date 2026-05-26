package com.example.education.system.schedule.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeacherAvailabilityResponse {
    private Integer code;
    private String message;
    private TeacherAvailabilityData data;

    @Data
    public static class TeacherAvailabilityData {
        private Integer teacherId;
        private String teacherName;
        private List<OccupiedSlot> occupiedSlots;
    }

    @Data
    public static class OccupiedSlot {
        private Integer scheduleId;
        private Integer courseId;
        private String courseName;
        private String classroom;
        private Integer dayOfWeek;
        private String startTime;
        private String endTime;
        private String semester;
        private Integer year;
    }
}
