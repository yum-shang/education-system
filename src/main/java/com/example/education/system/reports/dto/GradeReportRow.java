package com.example.education.system.reports.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class GradeReportRow {
    @ExcelProperty("学生姓名")
    private String studentName;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("成绩")
    private Double score;

    @ExcelProperty("等级")
    private String gradeLevel;

    @ExcelProperty("评语")
    private String comment;
}
