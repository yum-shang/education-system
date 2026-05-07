package com.example.education.system.courses.dto;

import com.example.education.system.courses.model.CourseEnrollment;
import lombok.Data;

import java.util.List;

@Data
public class EnrollmentListResponse {
    private Integer code;
    private String message;
    private List<CourseEnrollment> data;
    private Integer total;
    private Integer page;
    private Integer pageSize;
}
