package com.example.education.system.research.controller;

import com.example.education.system.research.dto.CreateProjectRequest;
import com.example.education.system.research.dto.ProjectListResponse;
import com.example.education.system.research.dto.ProjectApplicationRequest;
import com.example.education.system.research.service.ResearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/research-projects")
public class ResearchController {

    @Autowired
    private ResearchService researchService;

    @PostMapping
    public ProjectListResponse createProject(@RequestBody CreateProjectRequest request) {
        // 从JWT中获取教师ID
        Integer teacherId = 1; // 简化处理
        return researchService.createProject(request, teacherId);
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
    public ProjectListResponse applyProject(@RequestBody ProjectApplicationRequest request) {
        // 从JWT中获取学生ID
        Integer studentId = 1; // 简化处理
        return researchService.applyProject(request, studentId);
    }

    @PutMapping("/{applicationId}")
    public ProjectListResponse reviewApplication(
            @PathVariable Integer applicationId,
            @RequestParam String status) {
        return researchService.reviewApplication(applicationId, status);
    }

    @GetMapping
    public ProjectListResponse getApplicationList(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String status,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        return researchService.getApplicationList(projectId, status, page, pageSize);
    }
}