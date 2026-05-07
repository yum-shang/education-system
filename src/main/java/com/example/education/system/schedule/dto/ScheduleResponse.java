package com.example.education.system.schedule.dto;

import lombok.Data;

@Data
public class ScheduleResponse {
    private Integer code;
    private String message;
    private ScheduleData data;
    
    public static ScheduleResponse success(ScheduleData data) {
        ScheduleResponse response = new ScheduleResponse();
        response.setCode(200);
        response.setMessage("排课成功");
        response.setData(data);
        return response;
    }
    
    public static ScheduleResponse error(String message) {
        ScheduleResponse response = new ScheduleResponse();
        response.setCode(400);
        response.setMessage(message);
        return response;
    }

    @Data
    public static class ScheduleData {
        private Integer scheduleId;
        private Integer courseId;
        private String courseName;
        private Integer teacherId;
        private String teacherName;
        private String classroomName;
        private Integer dayOfWeek;
        private Integer timeSlot;
        private String startTime;
        private String endTime;
        private String semester;
        private Integer year;
    }
}
