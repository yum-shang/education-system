package com.example.education.system.research.repository;

import com.example.education.system.research.model.ApplicationWithStudent;
import com.example.education.system.research.model.ResearchProject;
import com.example.education.system.research.model.ProjectApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResearchRepository {
    void insertProject(ResearchProject project);

    List<ResearchProject> findProjects(@Param("status") String status, @Param("teacherId") Integer teacherId,
            @Param("keyword") String keyword, @Param("offset") Integer offset, @Param("limit") Integer limit);

    int countProjects(@Param("status") String status, @Param("teacherId") Integer teacherId,
            @Param("keyword") String keyword);

    String findProjectTeacherName(@Param("teacherId") Integer teacherId);

    ResearchProject findProjectById(@Param("projectId") Integer projectId);

    void insertApplication(ProjectApplication application);

    void updateApplicationStatus(@Param("applicationId") Integer applicationId, @Param("status") String status,
            @Param("reviewTime") java.sql.Timestamp reviewTime);

    void deleteApplication(@Param("applicationId") Integer applicationId);

    ProjectApplication findApplicationById(@Param("applicationId") Integer applicationId);

    ProjectApplication findApplicationByProjectAndStudent(@Param("projectId") Integer projectId,
            @Param("studentId") Integer studentId);

    void updateProjectStatus(@Param("projectId") Integer projectId, @Param("status") String status,
            @Param("createdAt") java.sql.Timestamp createdAt);

    List<ProjectApplication> findApplications(@Param("projectId") Integer projectId, @Param("status") String status,
            @Param("offset") Integer offset, @Param("limit") Integer limit);

    List<ApplicationWithStudent> findApplicationsWithStudent(
            @Param("projectId") Integer projectId,
            @Param("studentId") Integer studentId,
            @Param("teacherId") Integer teacherId,
            @Param("status") String status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countApplications(@Param("projectId") Integer projectId, @Param("studentId") Integer studentId,
            @Param("teacherId") Integer teacherId, @Param("status") String status);
}
