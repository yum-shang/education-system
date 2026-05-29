package com.example.education.system.grades.service;

import com.example.education.system.grades.dto.CreateGradeRequest;
import com.example.education.system.grades.dto.EnrolledStudentTemplateRow;
import com.example.education.system.grades.dto.GradeBatchResponse;
import com.example.education.system.grades.dto.GradeListResponse;
import com.example.education.system.grades.dto.GradeReportRow;
import com.example.education.system.grades.dto.GradeStats;
import com.example.education.system.grades.dto.GradeTrendItem;
import com.example.education.system.grades.model.Grade;
import com.example.education.system.grades.repository.GradeRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Transactional
    public GradeListResponse createGrade(CreateGradeRequest request, Integer teacherId) {
        if (teacherId == null) {
            throw new IllegalArgumentException("教师ID不能为空");
        }

        Grade grade = new Grade();
        grade.setEnrollmentId(request.getEnrollmentId());
        grade.setScore(request.getScore());
        grade.setGradeLevel(request.getGradeLevel());
        grade.setTeacherId(teacherId);
        grade.setComment(request.getComment());
        grade.setCreatedAt(new Timestamp(new Date().getTime()));

        gradeRepository.insertGrade(grade);

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("成绩录入成功");

        GradeListResponse.Data data = new GradeListResponse.Data();
        data.setList(new ArrayList<>());
        data.setTotal(0);
        data.setPage(1);
        data.setPageSize(10);

        response.setData(data);
        return response;
    }

    @Transactional
    public GradeListResponse updateGrade(Integer gradeId, CreateGradeRequest request) {
        Grade grade = gradeRepository.findGradeById(gradeId);
        if (grade != null) {
            grade.setScore(request.getScore());
            grade.setGradeLevel(request.getGradeLevel());
            grade.setComment(request.getComment());
            gradeRepository.updateGrade(grade);
        }

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("成绩修改成功");
        return response;
    }

    public GradeListResponse getTeacherGrades(Integer scheduleId, String gradeLevel, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<GradeListResponse.GradeInfo> gradeInfos = gradeRepository.findGradeInfoByScheduleId(scheduleId, gradeLevel, offset, pageSize);
        Integer total = gradeRepository.countGradeInfoByScheduleId(scheduleId, gradeLevel);

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        GradeListResponse.Data data = new GradeListResponse.Data();
        data.setList(gradeInfos);
        data.setTotal(total != null ? total : 0);
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    public GradeListResponse getTeacherGradeStats(Integer scheduleId) {
        List<GradeStats> stats = gradeRepository.countGradesByLevel(scheduleId);

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        GradeListResponse.Data data = new GradeListResponse.Data();
        data.setList(stats);
        data.setTotal(stats.size());
        data.setPage(1);
        data.setPageSize(stats.size());

        response.setData(data);
        return response;
    }

    public GradeListResponse getStudentGrades(Integer studentId, String semester, Integer year, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<GradeListResponse.GradeInfo> gradeInfos = gradeRepository.findGradeInfoByStudentId(studentId, semester, year, offset, pageSize);
        Integer total = gradeRepository.countGradeInfoByStudentId(studentId, semester, year);

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        GradeListResponse.Data data = new GradeListResponse.Data();
        data.setList(gradeInfos);
        data.setTotal(total != null ? total : 0);
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    public GradeListResponse getStudentGradeTrend(Integer studentId) {
        List<GradeTrendItem> trendItems = gradeRepository.avgScoreBySemester(studentId);

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        GradeListResponse.Data data = new GradeListResponse.Data();
        data.setList(trendItems);
        data.setTotal(trendItems.size());
        data.setPage(1);
        data.setPageSize(trendItems.size());

        response.setData(data);
        return response;
    }

    public void exportGradeReport(Integer scheduleId, HttpServletResponse response) throws IOException {
        List<GradeReportRow> rows = gradeRepository.findGradeReportByScheduleId(scheduleId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("成绩报表");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("学生姓名");
        headerRow.createCell(1).setCellValue("学号");
        headerRow.createCell(2).setCellValue("分数");
        headerRow.createCell(3).setCellValue("等级");
        headerRow.createCell(4).setCellValue("评语");

        int rowIndex = 1;
        for (GradeReportRow row : rows) {
            Row dataRow = sheet.createRow(rowIndex++);
            dataRow.createCell(0).setCellValue(row.getStudentName() != null ? row.getStudentName() : "");
            dataRow.createCell(1).setCellValue(row.getStudentNumber() != null ? row.getStudentNumber() : "");
            dataRow.createCell(2).setCellValue(row.getScore() != null ? row.getScore().doubleValue() : 0.0);
            dataRow.createCell(3).setCellValue(row.getGradeLevel() != null ? row.getGradeLevel() : "");
            dataRow.createCell(4).setCellValue(row.getComment() != null ? row.getComment() : "");
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        String fileName = "成绩报表_" + scheduleId + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void exportGradeTemplate(Integer scheduleId, HttpServletResponse response) throws IOException {
        List<EnrolledStudentTemplateRow> rows = gradeRepository.findEnrolledStudentsByScheduleId(scheduleId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("成绩模板");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("选课ID");
        headerRow.createCell(1).setCellValue("学号");
        headerRow.createCell(2).setCellValue("姓名");
        headerRow.createCell(3).setCellValue("分数");
        headerRow.createCell(4).setCellValue("等级");
        headerRow.createCell(5).setCellValue("评语");

        int rowIndex = 1;
        for (EnrolledStudentTemplateRow row : rows) {
            Row dataRow = sheet.createRow(rowIndex++);
            dataRow.createCell(0).setCellValue(row.getEnrollmentId() != null ? row.getEnrollmentId() : 0);
            dataRow.createCell(1).setCellValue(row.getStudentNumber() != null ? row.getStudentNumber() : "");
            dataRow.createCell(2).setCellValue(row.getStudentName() != null ? row.getStudentName() : "");
            dataRow.createCell(3).setCellValue(row.getScore() != null ? row.getScore().doubleValue() : 0.0);
            dataRow.createCell(4).setCellValue(row.getGradeLevel() != null ? row.getGradeLevel() : "");
            dataRow.createCell(5).setCellValue(row.getComment() != null ? row.getComment() : "");
        }

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        String fileName = "成绩模板_" + scheduleId + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Transactional
    public GradeBatchResponse batchImportGrades(Integer scheduleId, Integer teacherId, MultipartFile file) throws IOException {
        GradeBatchResponse result = new GradeBatchResponse();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int total = 0;

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    Double enrollmentIdVal = row.getCell(0) != null ? row.getCell(0).getNumericCellValue() : null;
                    if (enrollmentIdVal == null || enrollmentIdVal <= 0) continue;
                    Integer enrollmentId = enrollmentIdVal.intValue();

                    Double scoreVal = row.getCell(3) != null ? row.getCell(3).getNumericCellValue() : null;
                    if (scoreVal == null) {
                        errors.add("第" + (i + 1) + "行：分数为空，跳过");
                        continue;
                    }
                    if (scoreVal < 0 || scoreVal > 100) {
                        errors.add("第" + (i + 1) + "行：分数超出范围(0-100)，跳过");
                        continue;
                    }
                    double score = scoreVal;
                    String gradeLevel = getGradeLevel(score);

                    String comment = row.getCell(5) != null ? row.getCell(5).getStringCellValue() : "";

                    total++;

                    Grade grade = new Grade();
                    grade.setEnrollmentId(enrollmentId);
                    grade.setScore(score);
                    grade.setGradeLevel(gradeLevel);
                    grade.setTeacherId(teacherId);
                    grade.setComment(comment);
                    grade.setCreatedAt(new Timestamp(new Date().getTime()));

                    Grade existing = gradeRepository.findGradeByEnrollmentId(enrollmentId);
                    if (existing != null) {
                        existing.setScore(score);
                        existing.setGradeLevel(gradeLevel);
                        existing.setComment(comment);
                        gradeRepository.updateGrade(existing);
                    } else {
                        gradeRepository.insertGrade(grade);
                    }
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行：解析失败 - " + e.getMessage());
                }
            }

            workbook.close();
        }

        result.setTotal(total);
        result.setSuccessCount(successCount);
        result.setFailCount(total - successCount);
        result.setErrors(errors);
        return result;
    }

    private String getGradeLevel(double score) {
        if (score >= 90) return "excellent";
        if (score >= 80) return "good";
        if (score >= 70) return "average";
        if (score >= 60) return "pass";
        return "fail";
    }
}
