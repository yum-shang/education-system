package com.example.education.system.grades.dto;

import lombok.Data;

@Data
public class GradeBatchResponse {
    private Integer total;
    private Integer successCount;
    private Integer failCount;
    private java.util.List<String> errors;
}
