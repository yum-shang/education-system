package com.example.education.system.innovation.controller;

import com.example.education.system.innovation.dto.ApplicationListResponse;
import com.example.education.system.innovation.dto.CreateTeamRequest;
import com.example.education.system.innovation.dto.TeamApplicationRequest;
import com.example.education.system.innovation.dto.TeamListResponse;
import com.example.education.system.innovation.dto.UpdateTeamRequest;
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

    @PutMapping("/{teamId}")
    public TeamListResponse updateTeam(
            @PathVariable Integer teamId,
            @RequestBody UpdateTeamRequest request,
            HttpServletRequest httpRequest) {
        Integer userId = getCurrentStudentId(httpRequest);
        if (userId == null) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return innovationService.updateTeam(teamId, request, userId);
    }

    @GetMapping
    public TeamListResponse getTeamList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer leaderId,
            @RequestParam(required = false) String keyword,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        return innovationService.getTeamList(status, leaderId, keyword, page, pageSize);
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
            @RequestBody java.util.Map<String, String> body,
            HttpServletRequest httpRequest) {
        Integer userId = getCurrentStudentId(httpRequest);
        if (userId == null) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        String status = body != null ? body.get("status") : null;
        return innovationService.reviewTeamApplication(applicationId, status, userId);
    }

    @GetMapping
    public ApplicationListResponse getTeamApplicationList(
            @RequestParam(required = false) Integer teamId,
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) String status,
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            HttpServletRequest httpRequest) {
        if (teamId == null && studentId == null) {
            studentId = getCurrentStudentId(httpRequest);
        }
        return innovationService.getTeamApplicationList(teamId, studentId, status, page, pageSize);
    }

    private Integer getCurrentStudentId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return (Integer) userId;
        }
        return null;
    }
}
