package com.example.education.system.research.repository;

import com.example.education.system.research.model.ResearchProject;
import com.example.education.system.research.model.ProjectApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResearchRepository {
    void insertProject(ResearchProject project);
    List<ResearchProject> findProjects(@Param("status") String status, @Param("teacherId") Integer teacherId, @Param("offset") Integer offset, @Param("limit") Integer limit);
    ResearchProject findProjectById(@Param("projectId") Integer projectId);

    void insertApplication(ProjectApplication application);
    void updateApplicationStatus(@Param("applicationId") Integer applicationId, @Param("status") String status, @Param("reviewTime") java.sql.Timestamp reviewTime);
    List<ProjectApplication> findApplications(@Param("projectId") Integer projectId, @Param("status") String status, @Param("offset") Integer offset, @Param("limit") Integer limit);
}