package com.example.education.system.innovation.repository;

import com.example.education.system.innovation.dto.ApplicationListResponse;
import com.example.education.system.innovation.model.InnovationTeam;
import com.example.education.system.innovation.model.TeamApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface InnovationRepository {
    void insertTeam(InnovationTeam team);

    List<InnovationTeam> findTeams(@Param("status") String status, @Param("leaderId") Integer leaderId,
            @Param("keyword") String keyword, @Param("offset") Integer offset, @Param("limit") Integer limit);

    int countTeams(@Param("status") String status, @Param("leaderId") Integer leaderId,
            @Param("keyword") String keyword);

    String findTeamLeaderName(@Param("leaderId") Integer leaderId);

    InnovationTeam findTeamById(@Param("teamId") Integer teamId);

    InnovationTeam findTeamByName(@Param("teamName") String teamName);

    void updateTeam(@Param("teamId") Integer teamId, @Param("teamName") String teamName,
            @Param("projectName") String projectName, @Param("description") String description,
            @Param("status") String status);

    void insertTeamApplication(TeamApplication application);

    void updateTeamApplicationStatus(@Param("applicationId") Integer applicationId, @Param("status") String status,
            @Param("reviewTime") java.sql.Timestamp reviewTime);

    List<TeamApplication> findTeamApplications(@Param("teamId") Integer teamId, @Param("status") String status,
            @Param("offset") Integer offset, @Param("limit") Integer limit);

    List<ApplicationListResponse.ApplicationInfo> findTeamApplicationsWithInfo(
            @Param("teamId") Integer teamId, @Param("studentId") Integer studentId,
            @Param("status") String status, @Param("offset") Integer offset, @Param("limit") Integer limit);

    int countTeamApplications(@Param("teamId") Integer teamId, @Param("studentId") Integer studentId,
            @Param("status") String status);

    TeamApplication findTeamApplicationById(@Param("applicationId") Integer applicationId);

    TeamApplication findTeamApplicationByTeamAndStudent(@Param("teamId") Integer teamId,
            @Param("studentId") Integer studentId);
}
