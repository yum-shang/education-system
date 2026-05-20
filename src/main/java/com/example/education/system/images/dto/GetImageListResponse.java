package com.example.education.system.images.dto;

import lombok.Data;
import java.util.List;

@Data
public class GetImageListResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private List<ImageInfo> list;
        private Integer total;
        private Integer page;
        private Integer pageSize;
    }

    @lombok.Data
    public static class ImageInfo {
        private Integer imageId;
        private String filePath;
        private String fileName;
        private String fileType;
        private Integer userId;
        private String uploadTime;
    }
}
