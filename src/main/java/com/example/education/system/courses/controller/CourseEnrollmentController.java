package com.example.education.system.courses.controller;

import com.example.education.system.auth.service.JwtService;
import com.example.education.system.courses.dto.CreateEnrollmentRequest;
import com.example.education.system.courses.dto.EnrollmentInfoListResponse;
import com.example.education.system.courses.dto.EnrollmentListResponse;
import com.example.education.system.courses.dto.EnrolledStudentListResponse;
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
    public EnrollmentInfoListResponse getMyEnrollments(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {
        Integer userId = getUserIdFromToken(httpRequest);
        return courseEnrollmentService.getStudentEnrollmentsEnriched(userId, semester, year, page, pageSize);
    }

    @GetMapping("/schedule/{scheduleId}")
    public EnrollmentListResponse getCourseEnrollments(
            @PathVariable Integer scheduleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return courseEnrollmentService.getCourseEnrollments(scheduleId, page, pageSize);
    }

    @GetMapping("/available")
    public EnrollmentInfoListResponse getAvailableSchedules(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {
        Integer userId = getUserIdFromToken(httpRequest);
        return courseEnrollmentService.getAvailableSchedules(userId, semester, year, keyword, page, pageSize);
    }

    @GetMapping("/schedule/{scheduleId}/students")
    public EnrolledStudentListResponse getEnrolledStudents(
            @PathVariable Integer scheduleId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return courseEnrollmentService.getEnrolledStudents(scheduleId, keyword, page, pageSize);
    }
}
