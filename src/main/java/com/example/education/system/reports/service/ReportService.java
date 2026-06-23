package com.example.education.system.reports.service;

import com.alibaba.excel.EasyExcel;
import com.example.education.system.common.ApiResponse;
import com.example.education.system.reports.dto.*;
import com.example.education.system.reports.model.Report;
import com.example.education.system.reports.repository.ReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ReportRepository reportRepository;

    @Value("${report.storage.path:./uploads/reports}")
    private String reportStoragePath;

    @Transactional
    public ApiResponse<ReportGenerateResponse> generateGradesReport(GradesReportRequest request, Integer userId) {
        logger.info("开始生成成绩报表, userId={}, request={}", userId, request);

        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            logger.error("序列化请求参数失败", e);
            return ApiResponse.error(500, "请求参数处理失败");
        }

        Report existing = reportRepository.findByTypeAndContent("grades", contentJson);
        if (existing != null) {
            logger.info("成绩报表已存在, reportId={}", existing.getReportId());
            ReportGenerateResponse data = buildGenerateResponse(existing);
            return ApiResponse.success("报表已存在，无需重复生成", data);
        }

        List<GradeReportRow> rows = reportRepository.findGradeReportData(
                request.getSemester(), request.getYear(), request.getCourseId());

        if (rows.isEmpty()) {
            logger.warn("成绩报表查询无数据, semester={}, year={}, courseId={}",
                    request.getSemester(), request.getYear(), request.getCourseId());
        }

        String fileName = "grades_" + request.getSemester() + "_" + request.getCourseId() + ".xlsx";
        String relativePath = "/reports/" + fileName;
        String absolutePath = reportStoragePath + File.separator + fileName;
        ensureDirectoryExists();

        EasyExcel.write(absolutePath, GradeReportRow.class).sheet("成绩报表").doWrite(rows);
        logger.info("成绩报表Excel文件生成成功, path={}", absolutePath);

        String reportName = request.getSemester() + "学期高级数据结构成绩报表";
        Report report = createReport(reportName, "grades", contentJson, userId, relativePath);
        reportRepository.insert(report);

        ReportGenerateResponse data = buildGenerateResponse(report);
        return ApiResponse.success("报表生成成功", data);
    }

    @Transactional
    public ApiResponse<ReportGenerateResponse> generateUsersReport(UsersReportRequest request, Integer userId) {
        logger.info("开始生成用户统计报表, userId={}, request={}", userId, request);

        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            logger.error("序列化请求参数失败", e);
            return ApiResponse.error(500, "请求参数处理失败");
        }

        Report existing = reportRepository.findByTypeAndContent("users", contentJson);
        if (existing != null) {
            logger.info("用户统计报表已存在, reportId={}", existing.getReportId());
            ReportGenerateResponse data = buildGenerateResponse(existing);
            return ApiResponse.success("报表已存在，无需重复生成", data);
        }

        List<UserReportRow> rows = reportRepository.findUserReportData(request.getRole(), request.getYear());

        if (rows.isEmpty()) {
            logger.warn("用户统计报表查询无数据, role={}, year={}", request.getRole(), request.getYear());
        }

        String fileName = "users_" + request.getYear() + "_" + request.getRole() + ".xlsx";
        String relativePath = "/reports/" + fileName;
        String absolutePath = reportStoragePath + File.separator + fileName;
        ensureDirectoryExists();

        EasyExcel.write(absolutePath, UserReportRow.class).sheet("用户统计报表").doWrite(rows);
        logger.info("用户统计报表Excel文件生成成功, path={}", absolutePath);

        String reportName = request.getYear() + "年" + getRoleDisplayName(request.getRole()) + "统计报表";
        Report report = createReport(reportName, "users", contentJson, userId, relativePath);
        reportRepository.insert(report);

        ReportGenerateResponse data = buildGenerateResponse(report);
        return ApiResponse.success("报表生成成功", data);
    }

    public ApiResponse<ReportListResponse> getReportList(Integer page, Integer pageSize, String reportType) {
        int offset = (page - 1) * pageSize;
        List<Report> reports;
        Long total;

        if (reportType != null && !reportType.isEmpty()) {
            reports = reportRepository.findByType(reportType, offset, pageSize);
            total = reportRepository.countByType(reportType);
        } else {
            reports = reportRepository.findAll(offset, pageSize);
            total = reportRepository.countAll();
        }

        List<ReportListResponse.ReportInfo> list = reports.stream().map(r -> {
            ReportListResponse.ReportInfo info = new ReportListResponse.ReportInfo();
            info.setReportId(r.getReportId());
            info.setReportName(r.getReportName());
            info.setReportType(r.getReportType());
            info.setGeneratedAt(r.getGeneratedAt() != null ? r.getGeneratedAt().toString() : null);
            return info;
        }).collect(Collectors.toList());

        ReportListResponse data = new ReportListResponse();
        data.setList(list);
        data.setTotal(total);
        data.setPage(page);
        data.setPageSize(pageSize);

        return ApiResponse.success("获取成功", data);
    }

    private Report createReport(String reportName, String reportType, String content,
                                Integer userId, String filePath) {
        Report report = new Report();
        report.setReportName(reportName);
        report.setReportType(reportType);
        report.setContent(content);
        report.setGeneratedBy(userId);
        report.setFilePath(filePath);
        report.setGeneratedAt(new Timestamp(new Date().getTime()));
        return report;
    }

    private ReportGenerateResponse buildGenerateResponse(Report report) {
        ReportGenerateResponse response = new ReportGenerateResponse();
        response.setReportId(report.getReportId());
        response.setReportName(report.getReportName());
        response.setReportType(report.getReportType());
        response.setFilePath(report.getFilePath());
        return response;
    }

    private void ensureDirectoryExists() {
        File dir = new File(reportStoragePath);
        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("报表存储目录已创建: {}", dir.getAbsolutePath());
        }
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "admin": return "管理员";
            case "teacher": return "教师";
            case "student": return "学生";
            default: return role;
        }
    }
}
