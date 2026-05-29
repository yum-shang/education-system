package com.example.education.system.courses.dto;

import lombok.Data;
import java.util.List;

@Data
public class EnrollmentInfoListResponse {
    private Integer code;
    private String message;
    private List<EnrollmentInfo> data;
    private Integer total;
    private Integer page;
    private Integer pageSize;
}
