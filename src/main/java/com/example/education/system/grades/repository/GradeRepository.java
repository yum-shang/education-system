package com.example.education.system.grades.repository;

import com.example.education.system.grades.dto.EnrolledStudentTemplateRow;
import com.example.education.system.grades.dto.GradeListResponse;
import com.example.education.system.grades.dto.GradeReportRow;
import com.example.education.system.grades.dto.GradeStats;
import com.example.education.system.grades.dto.GradeTrendItem;
import com.example.education.system.grades.dto.StudentGradeReportRow;
import com.example.education.system.grades.model.Grade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GradeRepository {
    void insertGrade(Grade grade);
    void updateGrade(Grade grade);
    Grade findGradeById(@Param("gradeId") Integer gradeId);
    List<Grade> findGradesByScheduleId(@Param("scheduleId") Integer scheduleId, @Param("gradeLevel") String gradeLevel, @Param("offset") Integer offset, @Param("limit") Integer limit);
    List<Grade> findGradesByStudentId(@Param("studentId") Integer studentId, @Param("semester") String semester, @Param("year") Integer year, @Param("offset") Integer offset, @Param("limit") Integer limit);
    List<GradeReportRow> findGradeReportByScheduleId(@Param("scheduleId") Integer scheduleId);

    List<GradeListResponse.GradeInfo> findGradeInfoByScheduleId(@Param("scheduleId") Integer scheduleId, @Param("gradeLevel") String gradeLevel, @Param("offset") Integer offset, @Param("limit") Integer limit);
    Integer countGradeInfoByScheduleId(@Param("scheduleId") Integer scheduleId, @Param("gradeLevel") String gradeLevel);
    List<GradeStats> countGradesByLevel(@Param("scheduleId") Integer scheduleId);

    List<GradeListResponse.GradeInfo> findGradeInfoByStudentId(@Param("studentId") Integer studentId, @Param("semester") String semester, @Param("year") Integer year, @Param("offset") Integer offset, @Param("limit") Integer limit);
    Integer countGradeInfoByStudentId(@Param("studentId") Integer studentId, @Param("semester") String semester, @Param("year") Integer year);

    List<GradeTrendItem> avgScoreBySemester(@Param("studentId") Integer studentId);

    List<EnrolledStudentTemplateRow> findEnrolledStudentsByScheduleId(@Param("scheduleId") Integer scheduleId);

    Grade findGradeByEnrollmentId(@Param("enrollmentId") Integer enrollmentId);

    List<StudentGradeReportRow> findStudentGradeReport(@Param("studentId") Integer studentId, @Param("semester") String semester, @Param("year") Integer year);
}
