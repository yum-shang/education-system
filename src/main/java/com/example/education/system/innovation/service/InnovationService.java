package com.example.education.system.innovation.service;

import com.example.education.system.innovation.dto.CreateTeamRequest;
import com.example.education.system.innovation.dto.TeamListResponse;
import com.example.education.system.innovation.dto.TeamApplicationRequest;
import com.example.education.system.innovation.model.InnovationTeam;
import com.example.education.system.innovation.model.TeamApplication;
import com.example.education.system.innovation.repository.InnovationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 大创赛组队服务
 * 
 * 负责大学生创新创业大赛的组队管理，包括：
 * - 创建参赛队伍
 * - 队伍招募状态管理
 * - 学生申请加入队伍
 * - 队长审核入队申请
 * - 队伍列表查询
 * - 入队申请列表查询
 */
@Service
public class InnovationService {

    @Autowired
    private InnovationRepository innovationRepository;

    @Transactional
    public TeamListResponse createTeam(CreateTeamRequest request, Integer leaderId) {
        if (request.getTeamName() == null || request.getTeamName().trim().isEmpty()) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(400);
            response.setMessage("队伍名称不能为空");
            
            TeamListResponse.Data data = new TeamListResponse.Data();
            data.setList(new ArrayList<>());
            data.setTotal(0);
            data.setPage(1);
            data.setPageSize(10);
            response.setData(data);
            
            return response;
        }
        
        InnovationTeam existingTeam = innovationRepository.findTeamByName(request.getTeamName());
        if (existingTeam != null) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(409);
            response.setMessage("队伍名称已存在，请使用其他名称");
            
            TeamListResponse.Data data = new TeamListResponse.Data();
            data.setList(new ArrayList<>());
            data.setTotal(0);
            data.setPage(1);
            data.setPageSize(10);
            response.setData(data);
            
            return response;
        }
        
        InnovationTeam team = new InnovationTeam();
        team.setTeamName(request.getTeamName());
        team.setProjectName(request.getProjectName());
        team.setLeaderId(leaderId);
        team.setStatus("recruiting");
        team.setDescription(request.getDescription());
        team.setCreatedAt(new Timestamp(new Date().getTime()));

        innovationRepository.insertTeam(team);

        TeamListResponse response = new TeamListResponse();
        response.setCode(200);
        response.setMessage("队伍创建成功");

        TeamListResponse.Data data = new TeamListResponse.Data();
        data.setList(new ArrayList<>());
        data.setTotal(0);
        data.setPage(1);
        data.setPageSize(10);

        response.setData(data);
        return response;
    }

    public TeamListResponse getTeamList(String status, Integer leaderId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<InnovationTeam> teams = innovationRepository.findTeams(status, leaderId, offset, pageSize);

        TeamListResponse response = new TeamListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        TeamListResponse.Data data = new TeamListResponse.Data();
        List<TeamListResponse.TeamInfo> teamInfos = new ArrayList<>();

        for (InnovationTeam team : teams) {
            TeamListResponse.TeamInfo info = new TeamListResponse.TeamInfo();
            info.setTeamId(team.getTeamId());
            info.setTeamName(team.getTeamName());
            info.setProjectName(team.getProjectName());
            info.setLeaderId(team.getLeaderId());
            info.setStatus(team.getStatus());
            info.setDescription(team.getDescription());
            info.setCreatedAt(team.getCreatedAt().toString());
            teamInfos.add(info);
        }

        data.setList(teamInfos);
        data.setTotal(0); // 实际应该查询总数
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    @Transactional
    public TeamListResponse applyTeam(TeamApplicationRequest request, Integer studentId) {
        TeamApplication application = new TeamApplication();
        application.setTeamId(request.getTeamId());
        application.setStudentId(studentId);
        application.setApplicationLetter(request.getApplicationLetter());
        application.setStatus("pending");
        application.setApplyTime(new Timestamp(new Date().getTime()));

        innovationRepository.insertTeamApplication(application);

        TeamListResponse response = new TeamListResponse();
        response.setCode(200);
        response.setMessage("申请成功");

        TeamListResponse.Data data = new TeamListResponse.Data();
        data.setList(new ArrayList<>());
        data.setTotal(0);
        data.setPage(1);
        data.setPageSize(10);

        response.setData(data);
        return response;
    }

    @Transactional
    public TeamListResponse reviewTeamApplication(Integer applicationId, String status) {
        innovationRepository.updateTeamApplicationStatus(applicationId, status, new Timestamp(new Date().getTime()));

        TeamListResponse response = new TeamListResponse();
        response.setCode(200);
        response.setMessage("审核成功");
        return response;
    }

    public TeamListResponse getTeamApplicationList(Integer teamId, String status, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<TeamApplication> applications = innovationRepository.findTeamApplications(teamId, status, offset, pageSize);

        TeamListResponse response = new TeamListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        TeamListResponse.Data data = new TeamListResponse.Data();
        data.setList(new ArrayList<>());
        data.setTotal(0);
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }
}