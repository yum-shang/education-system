package com.example.education.system.innovation.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class TeamApplication {
    private Integer applicationId;
    private Integer teamId;
    private Integer studentId;
    private String applicationLetter;
    private String status;
    private Timestamp applyTime;
    private Timestamp reviewTime;
}