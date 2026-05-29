package com.example.education.system.grades.dto;

import lombok.Data;

@Data
public class GradeTrendItem {
    private String semester;
    private Integer year;
    private Double avgScore;
    private Double totalCredit;
    private Integer courseCount;
}
