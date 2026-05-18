package com.example.education.system.images.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Image {
    private Integer imageId;
    private String filePath;
    private String fileName;
    private String fileType;
    private String type;
    private Integer userId;
    private LocalDateTime uploadTime;
}
