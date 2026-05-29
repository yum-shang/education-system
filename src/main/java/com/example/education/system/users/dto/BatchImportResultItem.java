package com.example.education.system.users.dto;

import lombok.Data;

@Data
public class BatchImportResultItem {
    private String username;
    private String name;
    private String studentNumber;
    private boolean success;
    private String errorMessage;
}
