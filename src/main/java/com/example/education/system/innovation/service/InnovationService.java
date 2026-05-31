package com.example.education.system.innovation.service;

import com.example.education.system.innovation.dto.ApplicationListResponse;
import com.example.education.system.innovation.dto.CreateTeamRequest;
import com.example.education.system.innovation.dto.TeamApplicationRequest;
import com.example.education.system.innovation.dto.TeamListResponse;
import com.example.education.system.innovation.dto.UpdateTeamRequest;
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
            return response;
        }

        InnovationTeam existingTeam = innovationRepository.findTeamByName(request.getTeamName());
        if (existingTeam != null) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(409);
            response.setMessage("队伍名称已存在，请使用其他名称");
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

        InnovationTeam created = innovationRepository.findTeamByName(request.getTeamName());

        TeamListResponse response = new TeamListResponse();
        response.setCode(200);
        response.setMessage("组队发起成功");

        TeamListResponse.Data data = new TeamListResponse.Data();
        List<TeamListResponse.TeamInfo> list = new ArrayList<>();
        if (created != null) {
            TeamListResponse.TeamInfo info = buildTeamInfo(created);
            list.add(info);
        }
        data.setList(list);
        data.setTotal(list.size());
        data.setPage(1);
        data.setPageSize(10);
        response.setData(data);

        return response;
    }

    @Transactional
    public TeamListResponse updateTeam(Integer teamId, UpdateTeamRequest request, Integer userId) {
        InnovationTeam team = innovationRepository.findTeamById(teamId);
        if (team == null) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(404);
            response.setMessage("队伍不存在");
            return response;
        }

        if (!team.getLeaderId().equals(userId)) {
            TeamListResponse response = new TeamListResponse();
            response.setCode(403);
            response.setMessage("只有队长才能修改队伍信息");
            return response;
        }

        innovationRepository.updateTeam(teamId, request.getTeamName(), request.getProjectName(),
                request.getDescription(), request.getStatus());

        InnovationTeam updated = innovationRepository.findTeamById(teamId);

        TeamListResponse response = new TeamListResponse();
        response.setCode(200);
        response.setMessage("队伍更新成功");

        TeamListResponse.Data data = new TeamListResponse.Data();
        List<TeamListResponse.TeamInfo> list = new ArrayList<>();
        if (updated != null) {
            list.add(buildTeamInfo(updated));
        }
        data.setList(list);
        data.setTotal(list.size());
        data.setPage(1);
        data.setPageSize(10);
        response.setData(data);

        return response;
    }

    public TeamListResponse getTeamList(String status, Integer leaderId, String keyword, Integer page,
            Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<InnovationTeam> teams = innovationRepository.findTeams(status, leaderId, keyword, offset, pageSize);
        int total = innovationRepository.countTeams(status, leaderId, keyword);

        TeamListResponse response = new TeamListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        TeamListResponse.Data data = new TeamListResponse.Data();
        List<TeamListResponse.TeamInfo> teamInfos = new ArrayList<>();

        for (InnovationTeam team : teams) {
            teamInfos.add(buildTeamInfo(team));
        }

        data.setList(teamInfos);
        data.setTotal(total);
        data.setPage(page);
        data.setPageSize(pageSize);
        response.setData(data);
        return response;
    }

    @Transactional
    public TeamListResponse applyTeam(TeamApplicationRequest request, Integer studentId) {
        TeamListResponse response = new TeamListResponse();

        InnovationTeam team = innovationRepository.findTeamById(request.getTeamId());
        if (team == null) {
            response.setCode(404);
            response.setMessage("队伍不存在");
            return response;
        }

        if (!"recruiting".equals(team.getStatus())) {
            response.setCode(400);
            response.setMessage("该队伍已关闭招募，无法申请");
            return response;
        }

        if (team.getLeaderId().equals(studentId)) {
            response.setCode(400);
            response.setMessage("不能申请自己创建的队伍");
            return response;
        }

        TeamApplication existing = innovationRepository.findTeamApplicationByTeamAndStudent(request.getTeamId(),
                studentId);
        if (existing != null) {
            if ("pending".equals(existing.getStatus())) {
                response.setCode(400);
                response.setMessage("你已向该队伍提交过申请，请等待审核");
                return response;
            }
            if ("approved".equals(existing.getStatus())) {
                response.setCode(400);
                response.setMessage("你已经是该队伍的成员，无需重复申请");
                return response;
            }
            if ("rejected".equals(existing.getStatus())) {
                response.setCode(400);
                response.setMessage("你的申请已被拒绝，无法重复申请");
                return response;
            }
        }

        TeamApplication application = new TeamApplication();
        application.setTeamId(request.getTeamId());
        application.setStudentId(studentId);
        application.setApplicationLetter(request.getApplicationLetter());
        application.setStatus("pending");
        application.setApplyTime(new Timestamp(new Date().getTime()));

        innovationRepository.insertTeamApplication(application);

        response.setCode(200);
        response.setMessage("申请提交成功");
        return response;
    }

    @Transactional
    public TeamListResponse reviewTeamApplication(Integer applicationId, String status, Integer userId) {
        TeamListResponse response = new TeamListResponse();

        TeamApplication targetApp = innovationRepository.findTeamApplicationById(applicationId);

        if (targetApp == null) {
            response.setCode(404);
            response.setMessage("申请不存在");
            return response;
        }

        InnovationTeam team = innovationRepository.findTeamById(targetApp.getTeamId());
        if (team == null || !team.getLeaderId().equals(userId)) {
            response.setCode(403);
            response.setMessage("只有队长才能审核申请");
            return response;
        }

        innovationRepository.updateTeamApplicationStatus(applicationId, status,
                new Timestamp(new Date().getTime()));

        response.setCode(200);
        response.setMessage("审核成功");
        return response;
    }

    public ApplicationListResponse getTeamApplicationList(Integer teamId, Integer studentId, String status,
            Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ApplicationListResponse.ApplicationInfo> applications = innovationRepository
                .findTeamApplicationsWithInfo(teamId, studentId, status, offset, pageSize);
        int total = innovationRepository.countTeamApplications(teamId, studentId, status);

        ApplicationListResponse response = new ApplicationListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        ApplicationListResponse.Data data = new ApplicationListResponse.Data();
        data.setList(applications);
        data.setTotal(total);
        data.setPage(page);
        data.setPageSize(pageSize);
        response.setData(data);
        return response;
    }

    private TeamListResponse.TeamInfo buildTeamInfo(InnovationTeam team) {
        TeamListResponse.TeamInfo info = new TeamListResponse.TeamInfo();
        info.setTeamId(team.getTeamId());
        info.setTeamName(team.getTeamName());
        info.setProjectName(team.getProjectName());
        info.setLeaderId(team.getLeaderId());
        info.setStatus(team.getStatus());
        info.setDescription(team.getDescription());
        info.setCreatedAt(team.getCreatedAt().toString());

        String leaderName = innovationRepository.findTeamLeaderName(team.getLeaderId());
        info.setLeaderName(leaderName != null ? leaderName : "未知");
        return info;
    }
}
