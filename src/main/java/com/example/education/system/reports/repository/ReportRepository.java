package com.example.education.system.reports.repository;

import com.example.education.system.reports.dto.GradeReportRow;
import com.example.education.system.reports.dto.UserReportRow;
import com.example.education.system.reports.model.Report;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReportRepository {

    @Insert("INSERT INTO reports (report_name, report_type, content, generated_by, file_path) " +
            "VALUES (#{reportName}, #{reportType}, #{content}, #{generatedBy}, #{filePath})")
    @Options(useGeneratedKeys = true, keyProperty = "reportId")
    int insert(Report report);

    @Select("SELECT * FROM reports WHERE report_type = #{reportType} AND content = #{content}")
    Report findByTypeAndContent(@Param("reportType") String reportType, @Param("content") String content);

    @Select("SELECT * FROM reports ORDER BY generated_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Report> findAll(@Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM reports")
    Long countAll();

    @Select("SELECT * FROM reports WHERE report_type = #{reportType} ORDER BY generated_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Report> findByType(@Param("reportType") String reportType,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM reports WHERE report_type = #{reportType}")
    Long countByType(@Param("reportType") String reportType);

    @Select("SELECT s.name AS studentName, s.student_number AS studentNo, " +
            "g.score, g.grade_level AS gradeLevel, g.comment " +
            "FROM grades g " +
            "JOIN course_enrollments ce ON g.enrollment_id = ce.enrollment_id " +
            "JOIN course_schedules cs ON ce.schedule_id = cs.schedule_id " +
            "JOIN students s ON ce.student_id = s.user_id " +
            "WHERE cs.semester = #{semester} AND cs.year = #{year} AND cs.course_id = #{courseId} " +
            "ORDER BY s.student_number")
    List<GradeReportRow> findGradeReportData(@Param("semester") String semester,
                                             @Param("year") Integer year,
                                             @Param("courseId") Integer courseId);

    @Select("SELECT role, YEAR(created_at) AS year, COUNT(*) AS count " +
            "FROM users " +
            "WHERE role = #{role} AND YEAR(created_at) = #{year} " +
            "GROUP BY role, YEAR(created_at)")
    List<UserReportRow> findUserReportData(@Param("role") String role,
                                           @Param("year") Integer year);
}
