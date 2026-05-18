package com.example.education.system.schedule.service;

import com.example.education.system.classroom.model.Classroom;
import com.example.education.system.classroom.repository.ClassroomRepository;
import com.example.education.system.courses.model.Course;
import com.example.education.system.courses.model.CourseSchedule;
import com.example.education.system.courses.repository.CourseRepository;
import com.example.education.system.schedule.dto.AutoScheduleRequest;
import com.example.education.system.schedule.dto.ScheduleResponse;
import com.example.education.system.users.model.Teacher;
import com.example.education.system.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * 智能排课服务
 * 
 * 负责自动排课的核心业务逻辑，包括：
 * - 智能自动排课（基于随机算法）
 * - 教师时间冲突检测
 * - 教室可用性检测
 * - 排课结果生成
 */
@Service
@Slf4j
public class ScheduleService {
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final String[] TIME_SLOT_START = {
        "08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00", "19:00"
    };
    
    private static final String[] TIME_SLOT_END = {
        "09:00", "10:00", "11:00", "12:00", "15:00", "16:00", "17:00", "18:00", "21:00"
    };
    
    @Transactional
    public ScheduleResponse autoSchedule(AutoScheduleRequest request) {
        try {
            Course course = courseRepository.findCourseById(request.getCourseId());
            if (course == null) {
                return ScheduleResponse.error("课程不存在");
            }
            
            Teacher teacher = userRepository.findTeacherById(request.getTeacherId());
            if (teacher == null) {
                return ScheduleResponse.error("教师不存在");
            }
            
            Random random = new Random();
            int dayOfWeek = 0;
            int timeSlot = 0;
            Classroom selectedClassroom = null;
            int maxAttempts = 100;
            int attempts = 0;
            
            while (attempts < maxAttempts) {
                dayOfWeek = random.nextInt(7) + 1;
                timeSlot = random.nextInt(9) + 1;
                
                List<Classroom> availableClassrooms = classroomRepository.findAvailableByTimeSlot(timeSlot);
                
                if (availableClassrooms.isEmpty()) {
                    attempts++;
                    continue;
                }
                
                for (Classroom classroom : availableClassrooms) {
                    CourseSchedule conflict = courseRepository.findScheduleByTeacherAndTime(
                        request.getTeacherId(), dayOfWeek, TIME_SLOT_START[timeSlot - 1], 
                        request.getSemester(), request.getYear()
                    );
                    
                    if (conflict == null) {
                        selectedClassroom = classroom;
                        break;
                    }
                }
                
                if (selectedClassroom != null) {
                    break;
                }
                attempts++;
            }
            
            if (selectedClassroom == null) {
                return ScheduleResponse.error("没有找到合适的时间和教室");
            }
            
            classroomRepository.updateTimeSlot(selectedClassroom.getClassroomId(), timeSlot, 1);
            
            CourseSchedule schedule = new CourseSchedule();
            schedule.setCourseId(request.getCourseId());
            schedule.setTeacherId(request.getTeacherId());
            schedule.setClassroom(selectedClassroom.getClassroomName());
            schedule.setDayOfWeek(dayOfWeek);
            schedule.setStartTime(TIME_SLOT_START[timeSlot - 1]);
            schedule.setEndTime(TIME_SLOT_END[timeSlot - 1]);
            schedule.setSemester(request.getSemester());
            schedule.setYear(request.getYear());
            courseRepository.insertSchedule(schedule);
            
            ScheduleResponse.ScheduleData data = new ScheduleResponse.ScheduleData();
            data.setScheduleId(schedule.getScheduleId());
            data.setCourseId(course.getCourseId());
            data.setCourseName(course.getCourseName());
            data.setTeacherId(teacher.getTeacherId());
            data.setTeacherName(teacher.getName());
            data.setClassroomName(selectedClassroom.getClassroomName());
            data.setDayOfWeek(dayOfWeek);
            data.setTimeSlot(timeSlot);
            data.setStartTime(TIME_SLOT_START[timeSlot - 1]);
            data.setEndTime(TIME_SLOT_END[timeSlot - 1]);
            data.setSemester(request.getSemester());
            data.setYear(request.getYear());
            
            return ScheduleResponse.success(data);
            
        } catch (Exception e) {
            log.error("自动排课失败", e);
            return ScheduleResponse.error("排课失败：" + e.getMessage());
        }
    }
}
