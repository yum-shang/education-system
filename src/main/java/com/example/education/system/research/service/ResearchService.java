package com.example.education.system.research.service;

import com.example.education.system.research.dto.ApplicationListResponse;
import com.example.education.system.research.dto.CreateProjectRequest;
import com.example.education.system.research.dto.ProjectApplicationRequest;
import com.example.education.system.research.dto.ProjectListResponse;
import com.example.education.system.research.model.ApplicationWithStudent;
import com.example.education.system.research.model.ProjectApplication;
import com.example.education.system.research.model.ResearchProject;
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
        return response;
    }

    public ProjectListResponse getProjectList(String status, Integer teacherId, String keyword, Integer page,
            Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ResearchProject> projects = researchRepository.findProjects(status, teacherId, keyword, offset, pageSize);
        int total = researchRepository.countProjects(status, teacherId, keyword);

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

            String teacherName = researchRepository.findProjectTeacherName(project.getTeacherId());
            info.setTeacherName(teacherName != null ? teacherName : "未知");
            projectInfos.add(info);
        }

        data.setList(projectInfos);
        data.setTotal(total);
        data.setPage(page);
        data.setPageSize(pageSize);
        response.setData(data);
        return response;
    }

    @Transactional
    public ProjectListResponse applyProject(ProjectApplicationRequest request, Integer studentId) {
        ProjectListResponse response = new ProjectListResponse();

        ResearchProject project = researchRepository.findProjectById(request.getProjectId());
        if (project == null) {
            response.setCode(404);
            response.setMessage("项目不存在");
            return response;
        }

        if (!"open".equals(project.getStatus())) {
            response.setCode(400);
            response.setMessage("该项目已关闭，无法申请");
            return response;
        }

        ProjectApplication existing = researchRepository.findApplicationByProjectAndStudent(request.getProjectId(),
                studentId);
        if (existing != null) {
            if ("pending".equals(existing.getStatus())) {
                response.setCode(400);
                response.setMessage("你已提交过申请，请等待审核");
                return response;
            }
            if ("approved".equals(existing.getStatus())) {
                response.setCode(400);
                response.setMessage("你已被该项目录取，无需重复申请");
                return response;
            }
            if ("rejected".equals(existing.getStatus())) {
                response.setCode(400);
                response.setMessage("你的申请已被拒绝，无法重复申请");
                return response;
            }
        }

        ProjectApplication application = new ProjectApplication();
        application.setProjectId(request.getProjectId());
        application.setStudentId(studentId);
        application.setApplicationLetter(request.getApplicationLetter());
        application.setStatus("pending");
        application.setApplyTime(new Timestamp(new Date().getTime()));

        researchRepository.insertApplication(application);

        response.setCode(200);
        response.setMessage("申请提交成功");
        return response;
    }

    @Transactional
    public ProjectListResponse reviewApplication(Integer applicationId, String status, Integer userId) {
        ProjectListResponse response = new ProjectListResponse();

        ProjectApplication targetApp = researchRepository.findApplicationById(applicationId);
        if (targetApp == null) {
            response.setCode(404);
            response.setMessage("申请不存在");
            return response;
        }

        ResearchProject project = researchRepository.findProjectById(targetApp.getProjectId());
        if (project == null || !project.getTeacherId().equals(userId)) {
            response.setCode(403);
            response.setMessage("只有项目负责教师才能审核申请");
            return response;
        }

        researchRepository.updateApplicationStatus(applicationId, status,
                new Timestamp(new Date().getTime()));

        response.setCode(200);
        response.setMessage("审核成功");
        return response;
    }

    @Transactional
    public ProjectListResponse cancelApplication(Integer applicationId, Integer userId) {
        ProjectListResponse response = new ProjectListResponse();

        ProjectApplication targetApp = researchRepository.findApplicationById(applicationId);
        if (targetApp == null) {
            response.setCode(404);
            response.setMessage("申请不存在");
            return response;
        }

        if (!targetApp.getStudentId().equals(userId)) {
            response.setCode(403);
            response.setMessage("只能取消自己的申请");
            return response;
        }

        researchRepository.deleteApplication(applicationId);

        response.setCode(200);
        response.setMessage("报名已取消");
        return response;
    }

    public ApplicationListResponse getApplicationList(Integer projectId, Integer studentId, Integer teacherId,
            String status, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ApplicationWithStudent> applications = researchRepository.findApplicationsWithStudent(projectId, studentId,
                teacherId, status, offset, pageSize);
        int total = researchRepository.countApplications(projectId, studentId, teacherId, status);

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
        data.setTotal(total);
        data.setPage(page);
        data.setPageSize(pageSize);
        response.setData(data);
        return response;
    }

    @Transactional
    public ProjectListResponse updateProject(Integer projectId, String status) {
        if (projectId == null) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(400);
            response.setMessage("项目ID不能为空");
            return response;
        }

        if (status == null || status.isEmpty()) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(400);
            response.setMessage("状态不能为空");
            return response;
        }

        ResearchProject project = researchRepository.findProjectById(projectId);
        if (project == null) {
            ProjectListResponse response = new ProjectListResponse();
            response.setCode(404);
            response.setMessage("项目不存在");
            return response;
        }

        researchRepository.updateProjectStatus(projectId, status, new Timestamp(new Date().getTime()));

        ProjectListResponse response = new ProjectListResponse();
        response.setCode(200);
        response.setMessage("项目状态更新成功");
        return response;
    }
}
