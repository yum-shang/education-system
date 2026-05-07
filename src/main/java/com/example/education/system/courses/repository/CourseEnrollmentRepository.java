package com.example.education.system.courses.repository;

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
}
