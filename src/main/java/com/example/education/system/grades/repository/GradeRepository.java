package com.example.education.system.grades.repository;

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
}