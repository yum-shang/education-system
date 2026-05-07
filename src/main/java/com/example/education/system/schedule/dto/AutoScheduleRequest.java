package com.example.education.system.schedule.dto;

import lombok.Data;

@Data
public class AutoScheduleRequest {
    private String semester;
    private Integer year;
    private Integer courseId;
    private Integer teacherId;
}
