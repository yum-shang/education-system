package com.example.education.system.innovation.controller;

import com.example.education.system.innovation.dto.ApplicationListResponse;
import com.example.education.system.innovation.dto.CreateTeamRequest;
import com.example.education.system.innovation.dto.TeamApplicationRequest;
import com.example.education.system.innovation.dto.TeamListResponse;
import com.example.education.system.innovation.dto.UpdateTeamRequest;
import com.example.education.system.innovation.service.InnovationService;
import com.example.education.system.users.model.Student;
import com.example.education.system.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/innovation-teams")
public class InnovationController {

    @Autowired
    private InnovationService innovationService;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public TeamListResponse createTeam(@RequestBody CreateTeamRequest request, HttpServletRequest httpRequest) {
        Integer studentId = resolveStudentId(httpRequest);
        if (studentId == null) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return innovationService.createTeam(request, studentId);
    }

    @PutMapping("/{teamId}")
    public TeamListResponse updateTeam(
            @PathVariable Integer teamId,
            @RequestBody UpdateTeamRequest request,
            HttpServletRequest httpRequest) {
        Integer studentId = resolveStudentId(httpRequest);
        if (studentId == null) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        return innovationService.updateTeam(teamId, request, studentId);
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

    private Integer resolveStudentId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) return null;
        Student student = userRepository.findStudentByUserId((Integer) userId);
        return student != null ? student.getStudentId() : null;
    }
}

@RestController
@RequestMapping("/team-applications")
class TeamApplicationController {

    @Autowired
    private InnovationService innovationService;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public TeamListResponse applyTeam(@RequestBody TeamApplicationRequest request, HttpServletRequest httpRequest) {
        Integer studentId = resolveStudentId(httpRequest);
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
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        Integer studentId = resolveStudentId(httpRequest);
        if (studentId == null) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(401);
            response.setMessage("未登录或登录已过期");
            return response;
        }
        String status = body != null ? body.get("status") : null;
        return innovationService.reviewTeamApplication(applicationId, status, studentId);
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
            studentId = resolveStudentId(httpRequest);
        }
        return innovationService.getTeamApplicationList(teamId, studentId, status, page, pageSize);
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
}
