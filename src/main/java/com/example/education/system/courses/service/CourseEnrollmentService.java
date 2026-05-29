package com.example.education.system.courses.service;

import com.example.education.system.courses.dto.CreateEnrollmentRequest;
import com.example.education.system.courses.dto.EnrollmentInfo;
import com.example.education.system.courses.dto.EnrollmentInfoListResponse;
import com.example.education.system.courses.dto.EnrollmentListResponse;
import com.example.education.system.courses.dto.EnrolledStudentInfo;
import com.example.education.system.courses.dto.EnrolledStudentListResponse;
import com.example.education.system.courses.model.CourseEnrollment;
import com.example.education.system.courses.model.CourseSchedule;
import com.example.education.system.courses.repository.CourseEnrollmentRepository;
import com.example.education.system.courses.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 课程选课服务
 * 
 * 负责学生选课和退课的业务逻辑，包括：
 * - 学生选课（支持重复选课恢复）
 * - 学生退课
 * - 查询学生选课列表
 * - 查询课程的选课学生列表
 */
@Service
@Slf4j
public class CourseEnrollmentService {

    @Autowired
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public EnrollmentListResponse enrollCourse(Integer studentId, CreateEnrollmentRequest request) {
        EnrollmentListResponse response = new EnrollmentListResponse();

        CourseSchedule schedule = courseRepository.findScheduleById(request.getScheduleId());
        if (schedule == null) {
            response.setCode(400);
            response.setMessage("排课记录不存在");
            return response;
        }

        CourseEnrollment existing = courseEnrollmentRepository.findEnrollmentByStudentAndSchedule(studentId, request.getScheduleId());
        if (existing != null && Objects.equals(existing.getStatus(), "enrolled")) {
            response.setCode(400);
            response.setMessage("已选择该课程");
            response.setData(List.of(existing));
            return response;
        }
        else if(existing != null && Objects.equals(existing.getStatus(), "dropped")){
            //修改选课existing中的选课信息
            courseEnrollmentRepository.updateEnrollmentStatus(existing.getEnrollmentId(), "enrolled");
            response.setCode(200);
            response.setMessage("课程已经被再次选择");
            response.setData(List.of(existing));
            return response;
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudentId(studentId);
        enrollment.setScheduleId(request.getScheduleId());
        enrollment.setEnrollTime(LocalDateTime.now());
        enrollment.setStatus("enrolled");
        courseEnrollmentRepository.insertEnrollment(enrollment);

        response.setCode(200);
        response.setMessage("选课成功");
        response.setData(List.of(enrollment));
        return response;
    }

    public EnrollmentListResponse dropCourse(Integer studentId, Integer enrollmentId) {
        EnrollmentListResponse response = new EnrollmentListResponse();

        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findEnrollmentsByStudentId(studentId, null, null, 0, 1000);
        CourseEnrollment enrollment = enrollments.stream()
                .filter(e -> e.getEnrollmentId().equals(enrollmentId))
                .findFirst()
                .orElse(null);

        if (enrollment == null) {
            response.setCode(400);
            response.setMessage("选课记录不存在");
            return response;
        }

        if (!"enrolled".equals(enrollment.getStatus())) {
            response.setCode(400);
            response.setMessage("该课程已退课或状态异常");
            return response;
        }

        courseEnrollmentRepository.updateEnrollmentStatus(enrollmentId, "dropped");

        response.setCode(200);
        response.setMessage("退课成功");
        return response;
    }

    public EnrollmentListResponse getStudentEnrollments(Integer studentId, String semester, Integer year, Integer page, Integer pageSize) {
        EnrollmentListResponse response = new EnrollmentListResponse();

        int offset = (page - 1) * pageSize;
        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findEnrollmentsByStudentId(studentId, semester, year, offset, pageSize);
        Integer total = courseEnrollmentRepository.countEnrollmentsByStudentId(studentId, semester, year);

        response.setCode(200);
        response.setMessage("查询成功");
        response.setData(enrollments);
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);
        return response;
    }

    public EnrollmentInfoListResponse getStudentEnrollmentsEnriched(Integer studentId, String semester, Integer year, Integer page, Integer pageSize) {
        EnrollmentInfoListResponse response = new EnrollmentInfoListResponse();

        int offset = (page - 1) * pageSize;
        List<EnrollmentInfo> enrollments = courseEnrollmentRepository.findEnrollmentInfoByStudentId(studentId, semester, year, offset, pageSize);
        Integer total = courseEnrollmentRepository.countEnrollmentInfoByStudentId(studentId, semester, year);

        response.setCode(200);
        response.setMessage("查询成功");
        response.setData(enrollments);
        response.setTotal(total != null ? total : 0);
        response.setPage(page);
        response.setPageSize(pageSize);
        return response;
    }

    public EnrollmentListResponse getCourseEnrollments(Integer scheduleId, Integer page, Integer pageSize) {
        EnrollmentListResponse response = new EnrollmentListResponse();

        int offset = (page - 1) * pageSize;
        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findEnrollmentsByScheduleId(scheduleId, offset, pageSize);
        Integer total = courseEnrollmentRepository.countEnrollmentsByScheduleId(scheduleId);

        response.setCode(200);
        response.setMessage("查询成功");
        response.setData(enrollments);
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);
        return response;
    }

    public EnrollmentInfoListResponse getAvailableSchedules(Integer studentId, String semester, Integer year, String keyword, Integer page, Integer pageSize) {
        EnrollmentInfoListResponse response = new EnrollmentInfoListResponse();
        int offset = (page - 1) * pageSize;
        List<EnrollmentInfo> schedules = courseEnrollmentRepository.findAvailableSchedules(studentId, semester, year, keyword, offset, pageSize);
        Integer total = courseEnrollmentRepository.countAvailableSchedules(studentId, semester, year, keyword);
        response.setCode(200);
        response.setMessage("查询成功");
        response.setData(schedules);
        response.setTotal(total != null ? total : 0);
        response.setPage(page);
        response.setPageSize(pageSize);
        return response;
    }

    @Transactional
    public EnrollmentListResponse adminEnrollStudent(Integer studentId, Integer scheduleId) {
        CreateEnrollmentRequest req = new CreateEnrollmentRequest();
        req.setScheduleId(scheduleId);
        return enrollCourse(studentId, req);
    }

    public EnrollmentListResponse adminDropStudent(Integer enrollmentId) {
        EnrollmentListResponse response = new EnrollmentListResponse();
        courseEnrollmentRepository.deleteEnrollment(enrollmentId);
        response.setCode(200);
        response.setMessage("移除学生成功");
        return response;
    }

    public EnrolledStudentListResponse getEnrolledStudents(Integer scheduleId, String keyword, Integer page, Integer pageSize) {
        EnrolledStudentListResponse response = new EnrolledStudentListResponse();
        int offset = (page - 1) * pageSize;
        List<EnrolledStudentInfo> students = courseEnrollmentRepository.findEnrolledStudentsByScheduleId(scheduleId, keyword, offset, pageSize);
        Integer total = courseEnrollmentRepository.countEnrolledStudentsByScheduleId(scheduleId, keyword);
        response.setCode(200);
        response.setMessage("查询成功");
        response.setData(students);
        response.setTotal(total != null ? total : 0);
        response.setPage(page);
        response.setPageSize(pageSize);
        return response;
    }
}
