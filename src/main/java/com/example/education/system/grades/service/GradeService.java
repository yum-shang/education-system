package com.example.education.system.grades.service;

import com.example.education.system.grades.dto.CreateGradeRequest;
import com.example.education.system.grades.dto.GradeListResponse;
import com.example.education.system.grades.dto.GradeReportRow;
import com.example.education.system.grades.model.Grade;
import com.example.education.system.grades.repository.GradeRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 成绩管理服务
 * 
 * 负责学生成绩的录入和查询，包括：
 * - 教师录入学生成绩
 * - 教师修改学生成绩
 * - 教师查询课程成绩列表
 * - 学生查询个人成绩列表
 */
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
        List<Grade> grades = gradeRepository.findGradesByScheduleId(scheduleId, gradeLevel, offset, pageSize);

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        GradeListResponse.Data data = new GradeListResponse.Data();
        List<GradeListResponse.GradeInfo> gradeInfos = new ArrayList<>();

        for (Grade grade : grades) {
            GradeListResponse.GradeInfo info = new GradeListResponse.GradeInfo();
            info.setGradeId(grade.getGradeId());
            info.setScore(grade.getScore());
            info.setGradeLevel(grade.getGradeLevel());
            info.setComment(grade.getComment());
            info.setCreatedAt(grade.getCreatedAt().toString());
            gradeInfos.add(info);
        }

        data.setList(gradeInfos);
        data.setTotal(0); // 实际应该查询总数
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    public GradeListResponse getStudentGrades(Integer studentId, String semester, Integer year, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Grade> grades = gradeRepository.findGradesByStudentId(studentId, semester, year, offset, pageSize);

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        GradeListResponse.Data data = new GradeListResponse.Data();
        List<GradeListResponse.GradeInfo> gradeInfos = new ArrayList<>();

        for (Grade grade : grades) {
            GradeListResponse.GradeInfo info = new GradeListResponse.GradeInfo();
            info.setGradeId(grade.getGradeId());
            info.setScore(grade.getScore());
            info.setGradeLevel(grade.getGradeLevel());
            info.setComment(grade.getComment());
            gradeInfos.add(info);
        }

        data.setList(gradeInfos);
        data.setTotal(0); // 实际应该查询总数
        data.setPage(page);
        data.setPageSize(pageSize);

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
            dataRow.createCell(2).setCellValue(row.getScore() != null ? row.getScore() : 0);
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
}