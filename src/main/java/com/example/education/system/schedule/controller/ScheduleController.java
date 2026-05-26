package com.example.education.system.schedule.controller;

import com.example.education.system.classroom.model.Classroom;
import com.example.education.system.classroom.repository.ClassroomRepository;
import com.example.education.system.schedule.dto.AutoScheduleRequest;
import com.example.education.system.schedule.dto.BatchAutoScheduleRequest;
import com.example.education.system.schedule.dto.BatchScheduleResponse;
import com.example.education.system.schedule.dto.ClassroomAvailabilityResponse;
import com.example.education.system.schedule.dto.ScheduleResponse;
import com.example.education.system.schedule.dto.TeacherAvailabilityResponse;
import com.example.education.system.schedule.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @PostMapping("/auto")
    public ScheduleResponse autoSchedule(@RequestBody AutoScheduleRequest request) {
        return scheduleService.autoSchedule(request);
    }

    @PostMapping("/auto/batch")
    public BatchScheduleResponse autoScheduleBatch(@RequestBody BatchAutoScheduleRequest request) {
        return scheduleService.autoScheduleBatch(request);
    }

    @GetMapping("/teachers/{teacherId}/availability")
    public TeacherAvailabilityResponse getTeacherAvailability(
            @PathVariable Integer teacherId,
            @RequestParam String semester,
            @RequestParam Integer year) {
        return scheduleService.getTeacherAvailability(teacherId, semester, year);
    }

    @GetMapping("/classrooms/available")
    public ClassroomAvailabilityResponse getAvailableClassrooms(
            @RequestParam Integer dayOfWeek,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String semester,
            @RequestParam Integer year) {
        return scheduleService.getAvailableClassrooms(dayOfWeek, startTime, endTime, semester, year);
    }
    
    @GetMapping("/classrooms")
    public List<Classroom> getClassrooms() {
        return classroomRepository.findAll();
    }
    
    @GetMapping("/classrooms/{id}")
    public Classroom getClassroomById(@PathVariable Integer id) {
        return classroomRepository.findById(id);
    }
}
