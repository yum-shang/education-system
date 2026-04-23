package com.example.education.system.innovation.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class InnovationTeam {
    private Integer teamId;
    private String teamName;
    private String projectName;
    private Integer leaderId;
    private String status;
    private String description;
    private Timestamp createdAt;
}