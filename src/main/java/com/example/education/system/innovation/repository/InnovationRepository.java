package com.example.education.system.innovation.repository;

import com.example.education.system.innovation.model.InnovationTeam;
import com.example.education.system.innovation.model.TeamApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InnovationRepository {
    void insertTeam(InnovationTeam team);
    List<InnovationTeam> findTeams(@Param("status") String status, @Param("leaderId") Integer leaderId, @Param("offset") Integer offset, @Param("limit") Integer limit);
    InnovationTeam findTeamById(@Param("teamId") Integer teamId);

    void insertTeamApplication(TeamApplication application);
    void updateTeamApplicationStatus(@Param("applicationId") Integer applicationId, @Param("status") String status, @Param("reviewTime") java.sql.Timestamp reviewTime);
    List<TeamApplication> findTeamApplications(@Param("teamId") Integer teamId, @Param("status") String status, @Param("offset") Integer offset, @Param("limit") Integer limit);
}