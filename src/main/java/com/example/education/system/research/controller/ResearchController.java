package com.example.education.system.research.controller;

import com.example.education.system.research.dto.ApplicationListResponse;
import com.example.education.system.research.dto.CreateProjectRequest;
import com.example.education.system.research.dto.ProjectApplicationRequest;
import com.example.education.system.research.dto.ProjectListResponse;
import com.example.education.system.research.service.ResearchService;
import com.example.education.system.users.model.Student;
import com.example.education.system.users.model.Teacher;
import com.example.education.system.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/research-projects")
public class ResearchController {

    @Autowired
    private ResearchService researchService;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ProjectListResponse createProject(@RequestBody CreateProjectRequest request,
            HttpServletRequest httpRequest) {
        Integer teacherId = resolveTeacherId(httpRequest);
        if (teacherId == null) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return researchService.createProject(request, teacherId);
    }

    @PutMapping("/{projectId}")
    public ProjectListResponse updateProject(@PathVariable Integer projectId, @RequestParam String status) {
        return researchService.updateProject(projectId, status);
    }

    @GetMapping
    public ProjectListResponse getProjectList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer teacherId,
            @RequestParam(required = false) String keyword,
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            HttpServletRequest httpRequest) {
        if (teacherId == null && "teacher".equals(getCurrentRole(httpRequest))) {
            teacherId = resolveTeacherId(httpRequest);
        }
        return researchService.getProjectList(status, teacherId, keyword, page, pageSize);
    }

    private Integer resolveTeacherId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) return null;
        Teacher teacher = userRepository.findTeacherByUserId((Integer) userId);
        return teacher != null ? teacher.getTeacherId() : null;
    }

    private String getCurrentRole(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        if (role != null) {
            return (String) role;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                String authStr = authority.getAuthority();
                if (authStr.startsWith("ROLE_")) {
                    return authStr.substring(5).toLowerCase();
                }
            }
        }
        return null;
    }
}

@RestController
@RequestMapping("/project-applications")
class ProjectApplicationController {

    @Autowired
    private ResearchService researchService;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ProjectListResponse applyProject(@RequestBody ProjectApplicationRequest request,
            HttpServletRequest httpRequest) {
        Integer studentId = resolveStudentId(httpRequest);
        if (studentId == null) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return researchService.applyProject(request, studentId);
    }

    @PutMapping("/{applicationId}")
    public ProjectListResponse reviewApplication(@PathVariable Integer applicationId, @RequestParam String status,
            HttpServletRequest httpRequest) {
        Integer teacherId = resolveTeacherId(httpRequest);
        if (teacherId == null) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return researchService.reviewApplication(applicationId, status, teacherId);
    }

    @DeleteMapping("/{applicationId}")
    public ProjectListResponse cancelApplication(@PathVariable Integer applicationId,
            HttpServletRequest httpRequest) {
        Integer studentId = resolveStudentId(httpRequest);
        if (studentId == null) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return researchService.cancelApplication(applicationId, studentId);
    }

    @GetMapping
    public ApplicationListResponse getApplicationList(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) Integer teacherId,
            @RequestParam(required = false) String status,
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            HttpServletRequest httpRequest) {
        if (teacherId == null && "teacher".equals(getCurrentRole(httpRequest))) {
            teacherId = resolveTeacherId(httpRequest);
        }
        if (projectId == null && studentId == null && teacherId == null
                && "student".equals(getCurrentRole(httpRequest))) {
            studentId = resolveStudentId(httpRequest);
        }
        return researchService.getApplicationList(projectId, studentId, teacherId, status, page, pageSize);
    }

    /**
     * 从 JWT 获取 userId，查 Student 表得到自增 student_id。
     */
    private Integer resolveStudentId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) return null;
        Student student = userRepository.findStudentByUserId((Integer) userId);
        return student != null ? student.getStudentId() : null;
    }

    /**
     * 从 JWT 获取 userId，查 Teacher 表得到自增 teacher_id。
     */
    private Integer resolveTeacherId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) return null;
        Teacher teacher = userRepository.findTeacherByUserId((Integer) userId);
        return teacher != null ? teacher.getTeacherId() : null;
    }

    private String getCurrentRole(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        if (role != null) {
            return (String) role;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                String authStr = authority.getAuthority();
                if (authStr.startsWith("ROLE_")) {
                    return authStr.substring(5).toLowerCase();
                }
            }
        }
        return null;
    }
}
