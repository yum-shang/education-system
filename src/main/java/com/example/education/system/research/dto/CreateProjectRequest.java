package com.example.education.system.research.dto;

import lombok.Data;
import java.util.Date;

@Data
public class CreateProjectRequest {
    private String projectName;
    private String description;
    private Date startDate;
    private Date endDate;
}