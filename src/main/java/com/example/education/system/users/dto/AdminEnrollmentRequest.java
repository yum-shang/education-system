package com.example.education.system.users.dto;

import lombok.Data;

@Data
public class AdminEnrollmentRequest {
    private Integer studentId;
    private Integer scheduleId;
}
