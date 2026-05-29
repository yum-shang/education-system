package com.example.education.system.grades.controller;

import com.example.education.system.grades.dto.CreateGradeRequest;
import com.example.education.system.grades.dto.GradeBatchResponse;
import com.example.education.system.grades.dto.GradeListResponse;
import com.example.education.system.grades.service.GradeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @PostMapping
    public GradeListResponse createGrade(@RequestBody CreateGradeRequest request, HttpServletRequest httpRequest) {
        Integer teacherId = getCurrentTeacherId(httpRequest);
        if (teacherId == null) {
            GradeListResponse response = new GradeListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return gradeService.createGrade(request, teacherId);
    }

    private Integer getCurrentTeacherId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return (Integer) userId;
        }
        return null;
    }

    @PutMapping("/{gradeId}")
    public GradeListResponse updateGrade(
            @PathVariable Integer gradeId,
            @RequestBody CreateGradeRequest request) {
        return gradeService.updateGrade(gradeId, request);
    }

    @GetMapping("/report")
    public void downloadGradeReport(
            @RequestParam Integer scheduleId,
            @RequestParam String format,
            HttpServletResponse response) throws IOException {
        gradeService.exportGradeReport(scheduleId, response);
    }
}

@RestController
@RequestMapping("/teacher/grades")
class TeacherGradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping
    public GradeListResponse getTeacherGrades(
            @RequestParam Integer scheduleId,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        return gradeService.getTeacherGrades(scheduleId, gradeLevel, page, pageSize);
    }

    @GetMapping("/stats")
    public GradeListResponse getTeacherGradeStats(
            @RequestParam Integer scheduleId) {
        return gradeService.getTeacherGradeStats(scheduleId);
    }

    @GetMapping("/template")
    public void downloadTemplate(
            @RequestParam Integer scheduleId,
            HttpServletResponse response) throws IOException {
        gradeService.exportGradeTemplate(scheduleId, response);
    }

    @PostMapping("/batch")
    public GradeBatchResponse batchImport(
            @RequestParam Integer scheduleId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) throws IOException {
        Integer teacherId = getCurrentTeacherId(httpRequest);
        if (teacherId == null) {
            GradeBatchResponse response = new GradeBatchResponse();
            response.setTotal(0);
            response.setSuccessCount(0);
            response.setFailCount(0);
            response.setErrors(java.util.Collections.singletonList("未登录或登录已过期"));
            return response;
        }
        return gradeService.batchImportGrades(scheduleId, teacherId, file);
    }

    private Integer getCurrentTeacherId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return (Integer) userId;
        }
        return null;
    }
}

@RestController
@RequestMapping("/student/grades")
class StudentGradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping
    public GradeListResponse getStudentGrades(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer year,
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            HttpServletRequest request) {
        Integer studentId = getCurrentStudentId(request);
        if (studentId == null) {
            GradeListResponse response = new GradeListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return gradeService.getStudentGrades(studentId, semester, year, page, pageSize);
    }

    @GetMapping("/trend")
    public GradeListResponse getStudentGradeTrend(HttpServletRequest request) {
        Integer studentId = getCurrentStudentId(request);
        if (studentId == null) {
            GradeListResponse response = new GradeListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return gradeService.getStudentGradeTrend(studentId);
    }

    @GetMapping("/report")
    public void downloadReport(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer year,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Integer studentId = getCurrentStudentId(request);
        if (studentId == null) {
            response.setStatus(401);
            return;
        }
        gradeService.exportStudentGradeReport(studentId, semester, year, response);
    }

    private Integer getCurrentStudentId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return (Integer) userId;
        }
        return null;
    }
}
