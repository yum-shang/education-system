package com.example.education.system.grades.controller;

import com.example.education.system.grades.dto.CreateGradeRequest;
import com.example.education.system.grades.dto.GradeListResponse;
import com.example.education.system.grades.service.GradeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) Integer scheduleId,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer year,
            @RequestParam String format) {
        // 实现报表下载逻辑
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

    private Integer getCurrentStudentId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return (Integer) userId;
        }
        return null;
    }
}