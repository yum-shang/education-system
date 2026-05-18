package com.example.education.system.research.controller;

import com.example.education.system.research.dto.CreateProjectRequest;
import com.example.education.system.research.dto.ProjectListResponse;
import com.example.education.system.research.dto.ProjectApplicationRequest;
import com.example.education.system.research.dto.ApplicationListResponse;
import com.example.education.system.research.service.ResearchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/research-projects")
public class ResearchController {

    @Autowired
    private ResearchService researchService;

    @PostMapping
    public ProjectListResponse createProject(@RequestBody CreateProjectRequest request, HttpServletRequest httpRequest) {
        Integer teacherId = getCurrentTeacherId(httpRequest);
        if (teacherId == null) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return researchService.createProject(request, teacherId);
    }

    @PutMapping("/{projectId}")
    public ProjectListResponse updateProject(
            @PathVariable Integer projectId,
            @RequestParam String status) {
        if (status == null || status.isEmpty()) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(400);
            response.setMessage("状态不能为空");
            return response;
        }
        
        if (!"open".equals(status) && !"closed".equals(status)) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(400);
            response.setMessage("无效的状态值，只能是 open 或 closed");
            return response;
        }
        
        return researchService.updateProject(projectId, status);
    }

    private Integer getCurrentTeacherId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return (Integer) userId;
        }
        return null;
    }

    @GetMapping
    public ProjectListResponse getProjectList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer teacherId,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        return researchService.getProjectList(status, teacherId, page, pageSize);
    }
}

@RestController
@RequestMapping("/project-applications")
class ProjectApplicationController {

    @Autowired
    private ResearchService researchService;

    @PostMapping
    public ProjectListResponse applyProject(@RequestBody ProjectApplicationRequest request, HttpServletRequest httpRequest) {
        Integer studentId = getCurrentStudentId(httpRequest);
        if (studentId == null) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return researchService.applyProject(request, studentId);
    }

    private Integer getCurrentStudentId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return (Integer) userId;
        }
        return null;
    }

    @PutMapping("/{applicationId}")
    public ProjectListResponse reviewApplication(
            @PathVariable Integer applicationId,
            @RequestParam String status) {
        return researchService.reviewApplication(applicationId, status);
    }

    @GetMapping
    public ApplicationListResponse getApplicationList(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String status,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
            return researchService.getApplicationList(projectId, status, page, pageSize);
    }
}