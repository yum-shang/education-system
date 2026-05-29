package com.example.education.system.dashboard.service;

import com.example.education.system.auth.model.User;
import com.example.education.system.courses.model.Course;
import com.example.education.system.courses.repository.CourseRepository;
import com.example.education.system.dashboard.dto.DashboardStatsResponse;
import com.example.education.system.users.model.Student;
import com.example.education.system.users.model.Teacher;
import com.example.education.system.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private com.example.education.system.auth.repository.UserRepository authUserRepository;

    public DashboardStatsResponse getStats() {
        Integer totalUsers = userRepository.countUsers(null);
        Integer totalTeachers = userRepository.countUsers("teacher");

        List<Student> allStudents = userRepository.findStudents(null, null, null, 0, Integer.MAX_VALUE);
        Integer totalStudents = allStudents.size();

        List<Course> allCourses = courseRepository.findCourses(null, null, 0, Integer.MAX_VALUE);
        Integer totalCourses = allCourses.size();

        List<User> recentUsersRaw = userRepository.findRecentUsers(10);
        List<DashboardStatsResponse.RecentUser> recentUsers = new ArrayList<>();
        for (User user : recentUsersRaw) {
            DashboardStatsResponse.RecentUser ru = new DashboardStatsResponse.RecentUser();
            ru.setUserId(user.getUserId());
            ru.setUsername(user.getUsername());
            ru.setRole(user.getRole());
            ru.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

            if ("teacher".equals(user.getRole())) {
                Teacher teacher = userRepository.findTeacherById(user.getUserId());
                ru.setName(teacher != null ? teacher.getName() : user.getUsername());
            } else if ("student".equals(user.getRole())) {
                Student student = userRepository.findStudentById(user.getUserId());
                ru.setName(student != null ? student.getName() : user.getUsername());
            } else {
                ru.setName(user.getUsername());
            }

            recentUsers.add(ru);
        }

        List<DashboardStatsResponse.RecentCourse> recentCourses = new ArrayList<>();
        int courseLimit = Math.min(allCourses.size(), 5);
        for (int i = allCourses.size() - 1; i >= allCourses.size() - courseLimit && i >= 0; i--) {
            Course c = allCourses.get(i);
            DashboardStatsResponse.RecentCourse rc = new DashboardStatsResponse.RecentCourse();
            rc.setCourseId(c.getCourseId());
            rc.setCourseName(c.getCourseName());
            rc.setCourseCode(c.getCourseCode());
            rc.setCredit(c.getCredit());
            recentCourses.add(rc);
        }

        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        DashboardStatsResponse.Data data = new DashboardStatsResponse.Data();
        data.setTotalUsers(totalUsers);
        data.setTotalTeachers(totalTeachers);
        data.setTotalStudents(totalStudents);
        data.setTotalCourses(totalCourses);
        data.setRecentUsers(recentUsers);
        data.setRecentCourses(recentCourses);

        response.setData(data);
        return response;
    }
}
