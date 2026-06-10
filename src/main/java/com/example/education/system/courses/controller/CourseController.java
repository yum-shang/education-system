package com.example.education.system.courses.controller;

import com.example.education.system.courses.dto.CreateCourseRequest;
import com.example.education.system.courses.dto.CourseListResponse;
import com.example.education.system.courses.dto.SemesterInfo;
import com.example.education.system.courses.model.Course;
import com.example.education.system.courses.model.CourseSchedule;
import com.example.education.system.courses.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public CourseListResponse createCourse(@RequestBody CreateCourseRequest request) {
        return courseService.createCourse(request);
    }

    @GetMapping("/{courseId}")
    public CourseListResponse getCourseById(@PathVariable Integer courseId) {
        return courseService.getCourseById(courseId);
    }

    @GetMapping
    public CourseListResponse getCourseList(
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseCode,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        return courseService.getCourseList(courseName, courseCode, page, pageSize);
    }

    @PutMapping("/{courseId}")
    public CourseListResponse updateCourse(
            @PathVariable Integer courseId,
            @RequestBody CreateCourseRequest request) {
        return courseService.updateCourse(courseId, request);
    }

    @DeleteMapping("/{courseId}")
    public CourseListResponse deleteCourse(@PathVariable Integer courseId) {
        return courseService.deleteCourse(courseId);
    }
}

@RestController
@RequestMapping("/course-schedules")
class CourseScheduleController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public CourseListResponse createCourseSchedule(@RequestBody CourseSchedule schedule) {
        return courseService.createCourseSchedule(schedule);
    }

    @GetMapping
    public CourseListResponse getCourseScheduleList(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer teacherId,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer year,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        return courseService.getCourseScheduleList(courseId, teacherId, semester, year, page, pageSize);
    }

    @DeleteMapping("/{scheduleId}")
    public CourseListResponse deleteCourseSchedule(@PathVariable Integer scheduleId) {
        return courseService.deleteCourseSchedule(scheduleId);
    }

    @GetMapping("/semesters")
    public List<SemesterInfo> getSemesters() {
        return courseService.getDistinctSemesters();
    }
}