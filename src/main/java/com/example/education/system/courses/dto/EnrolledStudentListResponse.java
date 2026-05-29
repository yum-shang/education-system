package com.example.education.system.courses.dto;

import lombok.Data;
import java.util.List;

@Data
public class EnrolledStudentListResponse {
    private Integer code;
    private String message;
    private List<EnrolledStudentInfo> data;
    private Integer total;
    private Integer page;
    private Integer pageSize;
}
