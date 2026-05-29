package com.example.education.system.courses.repository;

import com.example.education.system.courses.dto.EnrollmentInfo;
import com.example.education.system.courses.dto.EnrolledStudentInfo;
import com.example.education.system.courses.model.CourseEnrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("courseEnrollmentRepository")
@Mapper
public interface CourseEnrollmentRepository {
    void insertEnrollment(CourseEnrollment enrollment);
    
    void updateEnrollmentStatus(@Param("enrollmentId") Integer enrollmentId, @Param("status") String status);
    
    List<CourseEnrollment> findEnrollmentsByStudentId(
            @Param("studentId") Integer studentId,
            @Param("semester") String semester,
            @Param("year") Integer year,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);
    
    Integer countEnrollmentsByStudentId(
            @Param("studentId") Integer studentId,
            @Param("semester") String semester,
            @Param("year") Integer year);
    
    List<CourseEnrollment> findEnrollmentsByScheduleId(
            @Param("scheduleId") Integer scheduleId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);
    
    Integer countEnrollmentsByScheduleId(@Param("scheduleId") Integer scheduleId);
    
    CourseEnrollment findEnrollmentByStudentAndSchedule(
            @Param("studentId") Integer studentId,
            @Param("scheduleId") Integer scheduleId);

    List<EnrollmentInfo> findEnrollmentInfoByStudentId(
            @Param("studentId") Integer studentId,
            @Param("semester") String semester,
            @Param("year") Integer year,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    Integer countEnrollmentInfoByStudentId(
            @Param("studentId") Integer studentId,
            @Param("semester") String semester,
            @Param("year") Integer year);

    List<EnrollmentInfo> findAvailableSchedules(
            @Param("studentId") Integer studentId,
            @Param("semester") String semester,
            @Param("year") Integer year,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    Integer countAvailableSchedules(
            @Param("studentId") Integer studentId,
            @Param("semester") String semester,
            @Param("year") Integer year,
            @Param("keyword") String keyword);

    List<EnrolledStudentInfo> findEnrolledStudentsByScheduleId(
            @Param("scheduleId") Integer scheduleId,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    Integer countEnrolledStudentsByScheduleId(
            @Param("scheduleId") Integer scheduleId,
            @Param("keyword") String keyword);

    void deleteEnrollment(@Param("enrollmentId") Integer enrollmentId);
}
