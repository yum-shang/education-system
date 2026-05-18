package com.example.education.system.research.service;

import com.example.education.system.research.dto.CreateProjectRequest;
import com.example.education.system.research.dto.ProjectListResponse;
import com.example.education.system.research.dto.ProjectApplicationRequest;
import com.example.education.system.research.dto.ApplicationListResponse;
import com.example.education.system.research.model.ResearchProject;
import com.example.education.system.research.model.ProjectApplication;
import com.example.education.system.research.model.ApplicationWithStudent;
import com.example.education.system.research.repository.ResearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 科研项目服务
 * 
 * 负责科研项目的发布和申请管理，包括：
 * - 教师发布科研项目
 * - 学生申请科研项目
 * - 教师审核项目申请
 * - 科研项目列表查询
 * - 项目申请列表查询
 */
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

    public ApplicationListResponse getApplicationList(Integer projectId, String status, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ApplicationWithStudent> applications = researchRepository.findApplicationsWithStudent(projectId, status, offset, pageSize);

        ApplicationListResponse response = new ApplicationListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        ApplicationListResponse.Data data = new ApplicationListResponse.Data();
        List<ApplicationListResponse.ApplicationInfo> applicationInfos = new ArrayList<>();

        for (ApplicationWithStudent app : applications) {
            ApplicationListResponse.ApplicationInfo info = new ApplicationListResponse.ApplicationInfo();
            info.setApplicationId(app.getApplicationId());
            info.setProjectId(app.getProjectId());
            info.setProjectName(app.getProjectName());
            info.setStudentId(app.getStudentId());
            info.setStudentName(app.getStudentName());
            info.setStudentNumber(app.getStudentNumber());
            info.setMajor(app.getMajor());
            info.setGrade(app.getGrade());
            info.setClazz(app.getClazz());
            info.setApplicationLetter(app.getApplicationLetter());
            info.setStatus(app.getStatus());
            info.setApplyTime(app.getApplyTime() != null ? app.getApplyTime().toString() : null);
            info.setReviewTime(app.getReviewTime() != null ? app.getReviewTime().toString() : null);
            applicationInfos.add(info);
        }

        data.setList(applicationInfos);
        data.setTotal(0); // 实际应该查询总数
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    @Transactional
    public ProjectListResponse updateProject(Integer projectId, String status) {
        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("状态不能为空");
        }
        
        ResearchProject project = researchRepository.findProjectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("项目不存在");
        }
        
        researchRepository.updateProjectStatus(projectId, status, new Timestamp(new Date().getTime()));
        
        ProjectListResponse response = new ProjectListResponse();
        response.setCode(200);
        response.setMessage("项目状态更新成功");
        return response;
    }
}