package com.example.education.system.innovation.dto;

import lombok.Data;

@Data
public class UpdateTeamRequest {
    private String teamName;
    private String projectName;
    private String description;
    private String status;
}
