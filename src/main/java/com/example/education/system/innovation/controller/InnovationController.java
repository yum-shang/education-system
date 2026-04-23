package com.example.education.system.innovation.controller;

import com.example.education.system.innovation.dto.CreateTeamRequest;
import com.example.education.system.innovation.dto.TeamListResponse;
import com.example.education.system.innovation.dto.TeamApplicationRequest;
import com.example.education.system.innovation.service.InnovationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/innovation-teams")
public class InnovationController {

    @Autowired
    private InnovationService innovationService;

    @PostMapping
    public TeamListResponse createTeam(@RequestBody CreateTeamRequest request) {
        // 从JWT中获取学生ID（队长）
        Integer leaderId = 1; // 简化处理
        return innovationService.createTeam(request, leaderId);
    }

    @GetMapping
    public TeamListResponse getTeamList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer leaderId,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        return innovationService.getTeamList(status, leaderId, page, pageSize);
    }
}

@RestController
@RequestMapping("/team-applications")
class TeamApplicationController {

    @Autowired
    private InnovationService innovationService;

    @PostMapping
    public TeamListResponse applyTeam(@RequestBody TeamApplicationRequest request) {
        // 从JWT中获取学生ID
        Integer studentId = 1; // 简化处理
        return innovationService.applyTeam(request, studentId);
    }

    @PutMapping("/{applicationId}")
    public TeamListResponse reviewTeamApplication(
            @PathVariable Integer applicationId,
            @RequestParam String status) {
        return innovationService.reviewTeamApplication(applicationId, status);
    }

    @GetMapping
    public TeamListResponse getTeamApplicationList(
            @RequestParam(required = false) Integer teamId,
            @RequestParam(required = false) String status,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        return innovationService.getTeamApplicationList(teamId, status, page, pageSize);
    }
}