package com.example.education.system.grades.dto;

import lombok.Data;

@Data
public class GradeBatchItem {
    private Integer enrollmentId;
    private Double score;
    private String comment;
}
