package com.example.education.system.courses.service;

import com.example.education.system.courses.dto.CreateCourseRequest;
import com.example.education.system.courses.dto.CourseListResponse;
import com.example.education.system.courses.model.Course;
import com.example.education.system.courses.model.CourseSchedule;
import com.example.education.system.courses.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程管理服务
 * 
 * 负责课程和课程安排的管理，包括：
 * - 课程创建、查询、更新、删除
 * - 课程安排（排课）管理
 * - 课程列表分页查询
 * - 课程安排列表查询
 */
@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public CourseListResponse createCourse(CreateCourseRequest request) {
        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setCredit(request.getCredit());
        course.setCourseCode(request.getCourseCode());
        course.setDescription(request.getDescription());

        courseRepository.insertCourse(course);

        CourseListResponse response = new CourseListResponse();
        response.setCode(200);
        response.setMessage("课程创建成功");

        CourseListResponse.Data data = new CourseListResponse.Data();
        data.setList(new ArrayList<>());
        data.setTotal(0);
        data.setPage(1);
        data.setPageSize(10);

        response.setData(data);
        return response;
    }

    public CourseListResponse getCourseList(String courseName, String courseCode, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Course> courses = courseRepository.findCourses(courseName, courseCode, offset, pageSize);

        CourseListResponse response = new CourseListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        CourseListResponse.Data data = new CourseListResponse.Data();
        List<CourseListResponse.CourseInfo> courseInfos = new ArrayList<>();

        for (Course course : courses) {
            CourseListResponse.CourseInfo info = new CourseListResponse.CourseInfo();
            info.setCourseId(course.getCourseId());
            info.setCourseName(course.getCourseName());
            info.setCredit(course.getCredit());
            info.setCourseCode(course.getCourseCode());
            info.setDescription(course.getDescription());
            courseInfos.add(info);
        }

        data.setList(courseInfos);
        data.setTotal(0); // 实际应该查询总数
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    @Transactional
    public CourseListResponse updateCourse(Integer courseId, CreateCourseRequest request) {
        Course course = courseRepository.findCourseById(courseId);
        if (course != null) {
            course.setCourseName(request.getCourseName());
            course.setCredit(request.getCredit());
            course.setCourseCode(request.getCourseCode());
            course.setDescription(request.getDescription());
            courseRepository.updateCourse(course);
        }

        CourseListResponse response = new CourseListResponse();
        response.setCode(200);
        response.setMessage("课程修改成功");
        return response;
    }

    @Transactional
    public CourseListResponse deleteCourse(Integer courseId) {
        courseRepository.deleteCourse(courseId);

        CourseListResponse response = new CourseListResponse();
        response.setCode(200);
        response.setMessage("课程删除成功");
        return response;
    }

    @Transactional
    public CourseListResponse createCourseSchedule(CourseSchedule schedule) {
        courseRepository.insertSchedule(schedule);

        CourseListResponse response = new CourseListResponse();
        response.setCode(200);
        response.setMessage("排课成功");
        return response;
    }

    public CourseListResponse getCourseScheduleList(Integer courseId, Integer teacherId, String semester, Integer year, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<CourseSchedule> schedules = courseRepository.findSchedules(courseId, teacherId, semester, year, offset, pageSize);

        CourseListResponse response = new CourseListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        CourseListResponse.Data data = new CourseListResponse.Data();
        List<CourseListResponse.ScheduleInfo> scheduleInfos = new ArrayList<>();

        for (CourseSchedule schedule : schedules) {
            CourseListResponse.ScheduleInfo info = new CourseListResponse.ScheduleInfo();
            info.setScheduleId(schedule.getScheduleId());
            info.setCourseId(schedule.getCourseId());
            info.setTeacherId(schedule.getTeacherId());
            info.setClassroom(schedule.getClassroom());
            info.setDayOfWeek(schedule.getDayOfWeek());
            info.setStartTime(schedule.getStartTime());
            info.setEndTime(schedule.getEndTime());
            info.setSemester(schedule.getSemester());
            info.setYear(schedule.getYear());
            
            if (schedule.getCourseId() != null) {
                Course course = courseRepository.findCourseById(schedule.getCourseId());
                if (course != null) {
                    info.setCourseName(course.getCourseName());
                    info.setCourseCode(course.getCourseCode());
                }
            }
            
            scheduleInfos.add(info);
        }

        data.setList(scheduleInfos);
        data.setTotal(scheduleInfos.size());
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }
}