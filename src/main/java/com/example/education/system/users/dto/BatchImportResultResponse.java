package com.example.education.system.users.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchImportResultResponse {
    private Integer code;
    private String message;
    private List<BatchImportResultItem> data;
}
