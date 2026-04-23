package com.example.education.system.innovation.dto;

import lombok.Data;
import java.util.List;

@Data
public class TeamListResponse {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private List<TeamInfo> list;
        private Integer total;
        private Integer page;
        private Integer pageSize;
    }

    @lombok.Data
    public static class TeamInfo {
        private Integer teamId;
        private String teamName;
        private String projectName;
        private Integer leaderId;
        private String leaderName;
        private String status;
        private String description;
        private String createdAt;
    }
}