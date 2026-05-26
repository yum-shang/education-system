package com.example.education.system.schedule.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClassroomAvailabilityResponse {
    private Integer code;
    private String message;
    private List<ClassroomInfo> data;

    @Data
    public static class ClassroomInfo {
        private Integer classroomId;
        private String classroomName;
        private Integer capacity;
        private Boolean available;
    }
}
