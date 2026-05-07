package com.example.education.system.courses.controller;

import com.example.education.system.auth.service.JwtService;
import com.example.education.system.courses.dto.CreateEnrollmentRequest;
import com.example.education.system.courses.dto.EnrollmentListResponse;
import com.example.education.system.courses.service.CourseEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/course-enrollments")
public class CourseEnrollmentController {

    @Autowired
    private CourseEnrollmentService courseEnrollmentService;

    @Autowired
    private JwtService jwtService;

    private Integer getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtService.getUserIdFromToken(token);
    }

    @PostMapping
    public EnrollmentListResponse enrollCourse(@RequestBody CreateEnrollmentRequest request, HttpServletRequest httpRequest) {
        Integer userId = getUserIdFromToken(httpRequest);
        return courseEnrollmentService.enrollCourse(userId, request);
    }

    @PutMapping("/{enrollmentId}/drop")
    public EnrollmentListResponse dropCourse(@PathVariable Integer enrollmentId, HttpServletRequest httpRequest) {
        Integer userId = getUserIdFromToken(httpRequest);
        return courseEnrollmentService.dropCourse(userId, enrollmentId);
    }

    @GetMapping("/my")
    public EnrollmentListResponse getMyEnrollments(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {
        Integer userId = getUserIdFromToken(httpRequest);
        return courseEnrollmentService.getStudentEnrollments(userId, semester, year, page, pageSize);
    }

    @GetMapping("/schedule/{scheduleId}")
    public EnrollmentListResponse getCourseEnrollments(
            @PathVariable Integer scheduleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return courseEnrollmentService.getCourseEnrollments(scheduleId, page, pageSize);
    }
}
