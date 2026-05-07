package com.example.education.system.schedule.controller;

import com.example.education.system.classroom.model.Classroom;
import com.example.education.system.classroom.repository.ClassroomRepository;
import com.example.education.system.schedule.dto.AutoScheduleRequest;
import com.example.education.system.schedule.dto.ScheduleResponse;
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
    
    @GetMapping("/classrooms")
    public List<Classroom> getClassrooms() {
        return classroomRepository.findAll();
    }
    
    @GetMapping("/classrooms/{id}")
    public Classroom getClassroomById(@PathVariable Integer id) {
        return classroomRepository.findById(id);
    }
}
