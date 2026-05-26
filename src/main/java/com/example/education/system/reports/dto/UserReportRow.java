package com.example.education.system.reports.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserReportRow {
    @ExcelProperty("角色")
    private String role;

    @ExcelProperty("年份")
    private Integer year;

    @ExcelProperty("人数")
    private Integer count;
}
