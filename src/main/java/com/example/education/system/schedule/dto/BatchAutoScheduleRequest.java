package com.example.education.system.schedule.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchAutoScheduleRequest {
    private String semester;
    private Integer year;
    private List<ScheduleItem> items;

    @Data
    public static class ScheduleItem {
        private Integer courseId;
        private Integer teacherId;
    }
}
