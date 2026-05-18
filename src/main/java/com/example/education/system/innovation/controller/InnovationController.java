package com.example.education.system.innovation.controller;

import com.example.education.system.innovation.dto.CreateTeamRequest;
import com.example.education.system.innovation.dto.TeamListResponse;
import com.example.education.system.innovation.dto.TeamApplicationRequest;
import com.example.education.system.innovation.service.InnovationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/innovation-teams")
public class InnovationController {

    @Autowired
    private InnovationService innovationService;

    @PostMapping
    public TeamListResponse createTeam(@RequestBody CreateTeamRequest request, HttpServletRequest httpRequest) {
        Integer leaderId = getCurrentStudentId(httpRequest);
        if (leaderId == null) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
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

    private Integer getCurrentStudentId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return (Integer) userId;
        }
        return null;
    }
}

@RestController
@RequestMapping("/team-applications")
class TeamApplicationController {

    @Autowired
    private InnovationService innovationService;

    @PostMapping
    public TeamListResponse applyTeam(@RequestBody TeamApplicationRequest request, HttpServletRequest httpRequest) {
        Integer studentId = getCurrentStudentId(httpRequest);
        if (studentId == null) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
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

    private Integer getCurrentStudentId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return (Integer) userId;
        }
        return null;
    }
}