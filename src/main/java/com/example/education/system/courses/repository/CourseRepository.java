package com.example.education.system.courses.repository;

import com.example.education.system.courses.model.Course;
import com.example.education.system.courses.model.CourseSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseRepository {
    void insertCourse(Course course);
    List<Course> findCourses(@Param("courseName") String courseName, @Param("courseCode") String courseCode, @Param("offset") Integer offset, @Param("limit") Integer limit);
    Course findCourseById(@Param("courseId") Integer courseId);
    void updateCourse(Course course);
    void deleteCourse(@Param("courseId") Integer courseId);

    void insertSchedule(CourseSchedule schedule);
    List<CourseSchedule> findSchedules(@Param("courseId") Integer courseId, @Param("teacherId") Integer teacherId, @Param("semester") String semester, @Param("year") Integer year, @Param("offset") Integer offset, @Param("limit") Integer limit);
    CourseSchedule findScheduleById(@Param("scheduleId") Integer scheduleId);
    void deleteSchedule(@Param("scheduleId") Integer scheduleId);
    
    CourseSchedule findScheduleByTeacherAndTime(@Param("teacherId") Integer teacherId, @Param("dayOfWeek") Integer dayOfWeek, 
                                                 @Param("startTime") String startTime, @Param("semester") String semester, 
                                                 @Param("year") Integer year);

    List<CourseSchedule> findSchedulesByTeacher(@Param("teacherId") Integer teacherId, @Param("semester") String semester, @Param("year") Integer year);

    List<String> findOccupiedClassrooms(@Param("dayOfWeek") Integer dayOfWeek, @Param("startTime") String startTime,
                                        @Param("endTime") String endTime, @Param("semester") String semester, @Param("year") Integer year);

    Integer countCourses(@Param("courseName") String courseName, @Param("courseCode") String courseCode);

    List<com.example.education.system.courses.dto.SemesterInfo> findDistinctSemesters();
}