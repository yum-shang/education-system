package com.example.education.system.dashboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardStatsResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private Integer totalUsers;
        private Integer totalTeachers;
        private Integer totalStudents;
        private Integer totalCourses;
        private List<RecentUser> recentUsers;
        private List<RecentCourse> recentCourses;
    }

    @lombok.Data
    public static class RecentUser {
        private Integer userId;
        private String username;
        private String name;
        private String role;
        private String createdAt;
    }

    @lombok.Data
    public static class RecentCourse {
        private Integer courseId;
        private String courseName;
        private String courseCode;
        private Double credit;
    }
}
