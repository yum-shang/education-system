package com.example.education.system.reports.controller;

import com.example.education.system.common.ApiResponse;
import com.example.education.system.reports.dto.*;
import com.example.education.system.reports.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @PostMapping("/grades")
    public ApiResponse<ReportGenerateResponse> generateGradesReport(
            @RequestBody GradesReportRequest request,
            HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未登录或登录已过期");
        }
        logger.info("收到成绩报表生成请求, userId={}", userId);
        return reportService.generateGradesReport(request, userId);
    }

    @PostMapping("/users")
    public ApiResponse<ReportGenerateResponse> generateUsersReport(
            @RequestBody UsersReportRequest request,
            HttpServletRequest httpRequest) {
        Integer userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未登录或登录已过期");
        }
        logger.info("收到用户统计报表生成请求, userId={}", userId);
        return reportService.generateUsersReport(request, userId);
    }

    @GetMapping
    public ApiResponse<ReportListResponse> getReportList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String reportType) {
        return reportService.getReportList(page, pageSize, reportType);
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId instanceof Integer) {
            return (Integer) userId;
        }
        return null;
    }
}
