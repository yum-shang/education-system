package com.example.education.system.images.dto;

import lombok.Data;

@Data
public class UploadImageRequest {
    private String filePath;
    private String fileName;
    private String fileType;
    private Integer userId;
}
