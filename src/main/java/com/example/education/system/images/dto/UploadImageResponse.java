package com.example.education.system.images.dto;

import lombok.Data;

@Data
public class UploadImageResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private Integer imageId;
        private String filePath;
        private String fileName;
        private String fileType;
    }
}
