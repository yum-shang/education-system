package com.example.education.system.research.service;

import com.example.education.system.research.dto.CreateProjectRequest;
import com.example.education.system.research.dto.ProjectListResponse;
import com.example.education.system.research.dto.ProjectApplicationRequest;
import com.example.education.system.research.model.ResearchProject;
import com.example.education.system.research.model.ProjectApplication;
import com.example.education.system.research.repository.ResearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ResearchService {

    @Autowired
    private ResearchRepository researchRepository;

    @Transactional
    public ProjectListResponse createProject(CreateProjectRequest request, Integer teacherId) {
        ResearchProject project = new ResearchProject();
        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setTeacherId(teacherId);
        project.setStatus("open");
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setCreatedAt(new Timestamp(new Date().getTime()));

        researchRepository.insertProject(project);

        ProjectListResponse response = new ProjectListResponse();
        response.setCode(200);
        response.setMessage("项目发布成功");

        ProjectListResponse.Data data = new ProjectListResponse.Data();
        data.setList(new ArrayList<>());
        data.setTotal(0);
        data.setPage(1);
        data.setPageSize(10);

        response.setData(data);
        return response;
    }

    public ProjectListResponse getProjectList(String status, Integer teacherId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ResearchProject> projects = researchRepository.findProjects(status, teacherId, offset, pageSize);

        ProjectListResponse response = new ProjectListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        ProjectListResponse.Data data = new ProjectListResponse.Data();
        List<ProjectListResponse.ProjectInfo> projectInfos = new ArrayList<>();

        for (ResearchProject project : projects) {
            ProjectListResponse.ProjectInfo info = new ProjectListResponse.ProjectInfo();
            info.setProjectId(project.getProjectId());
            info.setProjectName(project.getProjectName());
            info.setDescription(project.getDescription());
            info.setTeacherId(project.getTeacherId());
            info.setStatus(project.getStatus());
            info.setStartDate(project.getStartDate().toString());
            info.setEndDate(project.getEndDate().toString());
            info.setCreatedAt(project.getCreatedAt().toString());
            projectInfos.add(info);
        }

        data.setList(projectInfos);
        data.setTotal(0); // 实际应该查询总数
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    @Transactional
    public ProjectListResponse applyProject(ProjectApplicationRequest request, Integer studentId) {
        ProjectApplication application = new ProjectApplication();
        application.setProjectId(request.getProjectId());
        application.setStudentId(studentId);
        application.setApplicationLetter(request.getApplicationLetter());
        application.setStatus("pending");
        application.setApplyTime(new Timestamp(new Date().getTime()));

        researchRepository.insertApplication(application);

        ProjectListResponse response = new ProjectListResponse();
        response.setCode(200);
        response.setMessage("申请成功");

        ProjectListResponse.Data data = new ProjectListResponse.Data();
        data.setList(new ArrayList<>());
        data.setTotal(0);
        data.setPage(1);
        data.setPageSize(10);

        response.setData(data);
        return response;
    }

    @Transactional
    public ProjectListResponse reviewApplication(Integer applicationId, String status) {
        researchRepository.updateApplicationStatus(applicationId, status, new Timestamp(new Date().getTime()));

        ProjectListResponse response = new ProjectListResponse();
        response.setCode(200);
        response.setMessage("审核成功");
        return response;
    }

    public ProjectListResponse getApplicationList(Integer projectId, String status, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ProjectApplication> applications = researchRepository.findApplications(projectId, status, offset, pageSize);

        ProjectListResponse response = new ProjectListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        ProjectListResponse.Data data = new ProjectListResponse.Data();
        data.setList(new ArrayList<>());
        data.setTotal(0);
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }
}