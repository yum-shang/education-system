package com.example.education.system.users.controller;

import com.example.education.system.courses.dto.EnrollmentListResponse;
import com.example.education.system.courses.model.Course;
import com.example.education.system.courses.model.CourseEnrollment;
import com.example.education.system.courses.model.CourseSchedule;
import com.example.education.system.courses.repository.CourseRepository;
import com.example.education.system.courses.service.CourseEnrollmentService;
import com.example.education.system.users.dto.AdminEnrollmentRequest;
import com.example.education.system.users.dto.BatchImportStudentRequest;
import com.example.education.system.users.dto.BatchImportResultResponse;
import com.example.education.system.users.dto.CreateStudentRequest;
import com.example.education.system.users.dto.StudentEnrollmentResponse;
import com.example.education.system.users.dto.StudentListResponse;
import com.example.education.system.users.dto.UpdateStudentRequest;
import com.example.education.system.users.model.Teacher;
import com.example.education.system.users.repository.UserRepository;
import com.example.education.system.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseEnrollmentService courseEnrollmentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public StudentListResponse getStudentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String grade) {
        return userService.getStudentList(keyword, department, grade, page, pageSize);
    }

    @PostMapping
    public StudentListResponse createStudent(@RequestBody CreateStudentRequest request) {
        return userService.createStudent(request);
    }

    @PutMapping("/{userId}")
    public StudentListResponse updateStudent(
            @PathVariable Integer userId,
            @RequestBody UpdateStudentRequest request) {
        return userService.updateStudent(userId, request);
    }

    @DeleteMapping("/{userId}")
    public StudentListResponse deleteStudent(@PathVariable Integer userId) {
        return userService.deleteStudent(userId);
    }

    @GetMapping("/{userId}/enrollments")
    public StudentEnrollmentResponse getStudentEnrollments(
            @PathVariable Integer userId,
            @RequestParam(required = false) String semester) {

        EnrollmentListResponse enrollResponse = courseEnrollmentService.getStudentEnrollments(userId, semester, null, 1, 1000);

        StudentEnrollmentResponse response = new StudentEnrollmentResponse();
        response.setCode(enrollResponse.getCode());
        response.setMessage(enrollResponse.getMessage());

        List<StudentEnrollmentResponse.EnrollmentInfo> infos = new ArrayList<>();
        if (enrollResponse.getData() != null) {
            for (CourseEnrollment enrollment : enrollResponse.getData()) {
                StudentEnrollmentResponse.EnrollmentInfo info = new StudentEnrollmentResponse.EnrollmentInfo();
                info.setEnrollmentId(enrollment.getEnrollmentId());
                info.setScheduleId(enrollment.getScheduleId());
                info.setStatus(enrollment.getStatus());

                CourseSchedule schedule = courseRepository.findScheduleById(enrollment.getScheduleId());
                if (schedule != null) {
                    info.setClassroom(schedule.getClassroom());
                    info.setDayOfWeek(schedule.getDayOfWeek());
                    info.setStartTime(schedule.getStartTime());
                    info.setEndTime(schedule.getEndTime());
                    info.setSemester(schedule.getSemester());
                    info.setYear(schedule.getYear());

                    Course course = courseRepository.findCourseById(schedule.getCourseId());
                    if (course != null) {
                        info.setCourseName(course.getCourseName());
                        info.setCourseCode(course.getCourseCode());
                        info.setCredit(course.getCredit());
                    }

                    Teacher teacher = userRepository.findTeacherById(schedule.getTeacherId());
                    if (teacher != null) {
                        info.setTeacherName(teacher.getName());
                        info.setTeacherTitle(teacher.getTitle());
                        info.setTeacherDept(teacher.getDepartment());

                        com.example.education.system.auth.model.User teacherUser = userRepository.findUserById(schedule.getTeacherId());
                        if (teacherUser != null) {
                            info.setTeacherEmail(teacherUser.getEmail());
                        }
                    }
                }
                infos.add(info);
            }
        }
        response.setData(infos);
        return response;
    }

    @PostMapping("/batch")
    public BatchImportResultResponse batchImportStudents(@RequestBody BatchImportStudentRequest request) {
        return userService.batchImportStudents(request);
    }

    @PostMapping("/enrollments")
    public EnrollmentListResponse adminEnrollStudent(@RequestBody AdminEnrollmentRequest request) {
        return courseEnrollmentService.adminEnrollStudent(request.getStudentId(), request.getScheduleId());
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    public EnrollmentListResponse adminDropStudent(@PathVariable Integer enrollmentId) {
        return courseEnrollmentService.adminDropStudent(enrollmentId);
    }
}
