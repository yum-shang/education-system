package com.example.education.system.schedule.service;

import com.example.education.system.classroom.model.Classroom;
import com.example.education.system.classroom.repository.ClassroomRepository;
import com.example.education.system.courses.model.Course;
import com.example.education.system.courses.model.CourseSchedule;
import com.example.education.system.courses.repository.CourseRepository;
import com.example.education.system.schedule.dto.AutoScheduleRequest;
import com.example.education.system.schedule.dto.BatchAutoScheduleRequest;
import com.example.education.system.schedule.dto.BatchScheduleResponse;
import com.example.education.system.schedule.dto.ClassroomAvailabilityResponse;
import com.example.education.system.schedule.dto.ScheduleResponse;
import com.example.education.system.schedule.dto.TeacherAvailabilityResponse;
import com.example.education.system.users.model.Teacher;
import com.example.education.system.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 智能排课服务
 * 
 * 负责自动排课的核心业务逻辑，包括：
 * - 智能自动排课（基于随机算法）
 * - 教师时间冲突检测（基于 course_schedules 表精确查询）
 * - 教室可用性检测（基于 course_schedules 表精确查询）
 * - 排课结果生成
 * 
 * 冲突检测策略：
 * - 所有占用数据统一从 course_schedules 表查询，按 semester + year + day_of_week + 时间段精确判断
 * - classrooms 表仅存储基本信息（名称、容量），不再承担占用标记职责
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
            
            List<CourseSchedule> teacherSchedules = courseRepository.findSchedulesByTeacher(
                request.getTeacherId(), request.getSemester(), request.getYear()
            );
            Set<String> teacherBusySlots = new HashSet<>();
            for (CourseSchedule ts : teacherSchedules) {
                teacherBusySlots.add(ts.getDayOfWeek() + "-" + ts.getStartTime());
            }
            
            List<Classroom> allClassrooms = classroomRepository.findAll();
            Random random = new Random();
            int dayOfWeek = 0;
            int timeSlot = 0;
            String startTime = null;
            String endTime = null;
            Classroom selectedClassroom = null;
            int maxAttempts = 100;
            int attempts = 0;
            
            while (attempts < maxAttempts) {
                dayOfWeek = random.nextInt(7) + 1;
                timeSlot = random.nextInt(9) + 1;
                startTime = TIME_SLOT_START[timeSlot - 1];
                endTime = TIME_SLOT_END[timeSlot - 1];
                
                String key = dayOfWeek + "-" + startTime;
                if (teacherBusySlots.contains(key)) {
                    attempts++;
                    continue;
                }
                
                List<String> occupiedClassroomNames = courseRepository.findOccupiedClassrooms(
                    dayOfWeek, startTime, endTime, request.getSemester(), request.getYear()
                );
                Set<String> occupiedSet = new HashSet<>(occupiedClassroomNames);
                
                for (Classroom classroom : allClassrooms) {
                    if (!occupiedSet.contains(classroom.getClassroomName())) {
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
            
            CourseSchedule schedule = new CourseSchedule();
            schedule.setCourseId(request.getCourseId());
            schedule.setTeacherId(request.getTeacherId());
            schedule.setClassroom(selectedClassroom.getClassroomName());
            schedule.setDayOfWeek(dayOfWeek);
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
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
            data.setStartTime(startTime);
            data.setEndTime(endTime);
            data.setSemester(request.getSemester());
            data.setYear(request.getYear());
            
            return ScheduleResponse.success(data);
            
        } catch (Exception e) {
            log.error("自动排课失败", e);
            return ScheduleResponse.error("排课失败：" + e.getMessage());
        }
    }

    @Transactional
    public BatchScheduleResponse autoScheduleBatch(BatchAutoScheduleRequest request) {
        List<BatchScheduleResponse.ScheduleItemResult> results = new ArrayList<>();
        try {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                BatchScheduleResponse response = new BatchScheduleResponse();
                response.setCode(400);
                response.setMessage("排课列表不能为空，请使用 items 字段传入 [{courseId, teacherId}, ...]");
                response.setData(results);
                return response;
            }
            for (BatchAutoScheduleRequest.ScheduleItem item : request.getItems()) {
                BatchScheduleResponse.ScheduleItemResult result = new BatchScheduleResponse.ScheduleItemResult();
                result.setCourseId(item.getCourseId());
                result.setTeacherId(item.getTeacherId());

                Course course = courseRepository.findCourseById(item.getCourseId());
                if (course == null) {
                    result.setSuccess(false);
                    result.setErrorMessage("课程不存在");
                    results.add(result);
                    continue;
                }
                result.setCourseName(course.getCourseName());

                Teacher teacher = userRepository.findTeacherById(item.getTeacherId());
                if (teacher == null) {
                    result.setSuccess(false);
                    result.setErrorMessage("教师不存在");
                    results.add(result);
                    continue;
                }
                result.setTeacherName(teacher.getName());

                List<CourseSchedule> teacherSchedules = courseRepository.findSchedulesByTeacher(
                    item.getTeacherId(), request.getSemester(), request.getYear()
                );
                Set<String> teacherBusySlots = new HashSet<>();
                for (CourseSchedule ts : teacherSchedules) {
                    teacherBusySlots.add(ts.getDayOfWeek() + "-" + ts.getStartTime());
                }

                List<Classroom> allClassrooms = classroomRepository.findAll();
                Random random = new Random();
                int dayOfWeek = 0;
                int timeSlot = 0;
                String startTime = null;
                String endTime = null;
                Classroom selectedClassroom = null;
                int maxAttempts = 100;
                int attempts = 0;

                while (attempts < maxAttempts) {
                    dayOfWeek = random.nextInt(7) + 1;
                    timeSlot = random.nextInt(9) + 1;
                    startTime = TIME_SLOT_START[timeSlot - 1];
                    endTime = TIME_SLOT_END[timeSlot - 1];

                    String key = dayOfWeek + "-" + startTime;
                    if (teacherBusySlots.contains(key)) {
                        attempts++;
                        continue;
                    }

                    List<String> occupiedClassroomNames = courseRepository.findOccupiedClassrooms(
                        dayOfWeek, startTime, endTime, request.getSemester(), request.getYear()
                    );
                    Set<String> occupiedSet = new HashSet<>(occupiedClassroomNames);

                    for (Classroom classroom : allClassrooms) {
                        if (!occupiedSet.contains(classroom.getClassroomName())) {
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
                    result.setSuccess(false);
                    result.setErrorMessage("没有找到合适的时间和教室");
                    results.add(result);
                    continue;
                }

                CourseSchedule schedule = new CourseSchedule();
                schedule.setCourseId(item.getCourseId());
                schedule.setTeacherId(item.getTeacherId());
                schedule.setClassroom(selectedClassroom.getClassroomName());
                schedule.setDayOfWeek(dayOfWeek);
                schedule.setStartTime(startTime);
                schedule.setEndTime(endTime);
                schedule.setSemester(request.getSemester());
                schedule.setYear(request.getYear());
                courseRepository.insertSchedule(schedule);

                result.setScheduleId(schedule.getScheduleId());
                result.setClassroomName(selectedClassroom.getClassroomName());
                result.setDayOfWeek(dayOfWeek);
                result.setTimeSlot(timeSlot);
                result.setStartTime(startTime);
                result.setEndTime(endTime);
                result.setSemester(request.getSemester());
                result.setYear(request.getYear());
                result.setSuccess(true);
                results.add(result);
            }

            BatchScheduleResponse response = new BatchScheduleResponse();
            response.setCode(200);
            response.setMessage("批量排课完成");
            response.setData(results);
            return response;

        } catch (Exception e) {
            log.error("批量自动排课失败", e);
            BatchScheduleResponse response = new BatchScheduleResponse();
            response.setCode(400);
            response.setMessage("批量排课失败：" + e.getMessage());
            response.setData(results);
            return response;
        }
    }

    public TeacherAvailabilityResponse getTeacherAvailability(Integer teacherId, String semester, Integer year) {
        try {
            Teacher teacher = userRepository.findTeacherById(teacherId);
            if (teacher == null) {
                TeacherAvailabilityResponse response = new TeacherAvailabilityResponse();
                response.setCode(400);
                response.setMessage("教师不存在");
                return response;
            }

            List<CourseSchedule> schedules = courseRepository.findSchedulesByTeacher(teacherId, semester, year);

            TeacherAvailabilityResponse response = new TeacherAvailabilityResponse();
            response.setCode(200);
            response.setMessage("获取成功");

            TeacherAvailabilityResponse.TeacherAvailabilityData data = new TeacherAvailabilityResponse.TeacherAvailabilityData();
            data.setTeacherId(teacher.getTeacherId());
            data.setTeacherName(teacher.getName());

            List<TeacherAvailabilityResponse.OccupiedSlot> occupiedSlots = new ArrayList<>();
            for (CourseSchedule schedule : schedules) {
                TeacherAvailabilityResponse.OccupiedSlot slot = new TeacherAvailabilityResponse.OccupiedSlot();
                slot.setScheduleId(schedule.getScheduleId());
                slot.setCourseId(schedule.getCourseId());
                slot.setClassroom(schedule.getClassroom());
                slot.setDayOfWeek(schedule.getDayOfWeek());
                slot.setStartTime(schedule.getStartTime());
                slot.setEndTime(schedule.getEndTime());
                slot.setSemester(schedule.getSemester());
                slot.setYear(schedule.getYear());

                Course course = courseRepository.findCourseById(schedule.getCourseId());
                slot.setCourseName(course != null ? course.getCourseName() : null);

                occupiedSlots.add(slot);
            }
            data.setOccupiedSlots(occupiedSlots);
            response.setData(data);
            return response;

        } catch (Exception e) {
            log.error("获取教师空闲时段失败", e);
            TeacherAvailabilityResponse response = new TeacherAvailabilityResponse();
            response.setCode(400);
            response.setMessage("查询失败：" + e.getMessage());
            return response;
        }
    }

    public ClassroomAvailabilityResponse getAvailableClassrooms(Integer dayOfWeek, String startTime,
                                                                  String endTime, String semester, Integer year) {
        try {
            List<String> occupiedClassroomNames = courseRepository.findOccupiedClassrooms(
                dayOfWeek, startTime, endTime, semester, year
            );
            Set<String> occupiedSet = new HashSet<>(occupiedClassroomNames);

            List<Classroom> allClassrooms = classroomRepository.findAll();

            ClassroomAvailabilityResponse response = new ClassroomAvailabilityResponse();
            response.setCode(200);
            response.setMessage("获取成功");

            List<ClassroomAvailabilityResponse.ClassroomInfo> classroomInfos = new ArrayList<>();
            for (Classroom classroom : allClassrooms) {
                ClassroomAvailabilityResponse.ClassroomInfo info = new ClassroomAvailabilityResponse.ClassroomInfo();
                info.setClassroomId(classroom.getClassroomId());
                info.setClassroomName(classroom.getClassroomName());
                info.setCapacity(classroom.getCapacity());
                info.setAvailable(!occupiedSet.contains(classroom.getClassroomName()));
                classroomInfos.add(info);
            }
            response.setData(classroomInfos);
            return response;

        } catch (Exception e) {
            log.error("获取空闲教室失败", e);
            ClassroomAvailabilityResponse response = new ClassroomAvailabilityResponse();
            response.setCode(400);
            response.setMessage("查询失败：" + e.getMessage());
            return response;
        }
    }
}
