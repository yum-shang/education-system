package com.example.education.system.courses.tool;

import com.example.education.system.courses.dto.EnrollmentInfo;
import com.example.education.system.courses.model.Course;
import com.example.education.system.courses.repository.CourseEnrollmentRepository;
import com.example.education.system.courses.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 课程查询工具 — 供 AI Function Calling 调用。
 *
 * <h3>1003 事件传递机制</h3>
 * <p>
 * 通过 Spring AI 的 {@link ToolContext} 传递 {@code requestId}：
 * ChatServiceImpl 在发起流式请求时调用 {@code .toolContext(Map.of("requestId", requestId))}，
 * 工具方法从 context 中读取 requestId，将课程卡片写入对应请求的缓存，避免并发串台。
 */
@Component
@RequiredArgsConstructor
public class CourseQueryTools {

    /** 显式声明 Logger，避免 IDE 未启用 Lombok 注解处理时找不到 log 变量 */
    private static final Logger log = LoggerFactory.getLogger(CourseQueryTools.class);

    /** ToolContext 中存放当前 AI 请求 ID 的键名 */
    public static final String CONTEXT_REQUEST_ID = "requestId";

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    /**
     * 课程事件缓存：requestId → 本次请求查询到的课程卡片列表。
     */
    private final ConcurrentHashMap<String, List<Map<String, Object>>> courseEventCache = new ConcurrentHashMap<>();

    /**
     * 取出并移除指定 requestId 的课程事件。
     * ChatServiceImpl 在 AI 流结束后调用，防止内存泄漏。
     */
    public List<Map<String, Object>> takeCourseEvents(String requestId) {
        return courseEventCache.remove(requestId);
    }

    /**
     * 按课程 ID 精确查询课程信息。
     * 对应系统提示词技能1第6条："推荐课程必须通过 findCourseById 查询后，才能返回数据"
     */
    @Tool(description = "根据课程ID精确查询课程的详细信息，包括课程名称、学分、课程代码和课程描述")
    public Course findCourseById(
            @ToolParam(description = "课程ID，整数") Integer courseId,
            ToolContext toolContext) {

        Course course = courseRepository.findCourseById(courseId);
        String requestId = resolveRequestId(toolContext);
        log.debug("AI 调用 findCourseById, courseId={}, requestId={}", courseId, requestId);

        if (course != null && requestId != null) {
            Map<String, Object> courseData = new LinkedHashMap<>();
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", String.valueOf(course.getCourseId()));
            info.put("name", course.getCourseName());
            info.put("code", course.getCourseCode());
            info.put("credit", course.getCredit());
            info.put("description", course.getDescription());
            courseData.put("courseInfo_" + course.getCourseId(), info);

            // 仅写入当前 requestId 对应的缓存，不会污染其他并发请求
            courseEventCache.computeIfAbsent(requestId, k -> new ArrayList<>()).add(courseData);
            log.debug("1003课程数据已写入 requestId={}", requestId);
        }
        return course;
    }

    @Tool(description = "根据学生ID查询该学生的所有选修课程，返回课程名称、授课老师、开课年份")
    public List<Map<String, String>> getStudentEnrollments(
            @ToolParam(description = "学生ID，整数") Integer studentId,
            ToolContext toolContext) {

        List<EnrollmentInfo> enrollments = enrollmentRepository.findEnrollmentInfoByStudentId(
                studentId, null, null, 0, 100);
        log.debug("AI 调用 getStudentEnrollments, studentId={}, 结果数={}", studentId, enrollments.size());

        return enrollments.stream()
                .map(e -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("courseName", e.getCourseName());
                    item.put("teacherName", e.getTeacherName());
                    item.put("year", String.valueOf(e.getYear()));
                    return item;
                })
                .toList();
    }

    /**
     * 搜索课程（可选关键词）。
     * 不传 keyword 时返回数据库中全部课程，方便 AI 了解完整的课程库再做推荐。
     */
    @Tool(description = "搜索课程。不传参数时返回全部课程列表；传入关键词时按课程名称或代码模糊匹配（最多50条）")
    public List<Course> findCourses(
            @ToolParam(description = "搜索关键词（可选）。留空则返回全部课程", required = false) String keyword,
            ToolContext toolContext) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        return courseRepository.findCourses(kw, kw, 0, 50);
    }

    /**
     * 从 ToolContext 解析 requestId；context 缺失时返回 null（不写 1003 缓存）。
     */
    private String resolveRequestId(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            return null;
        }
        Object value = toolContext.getContext().get(CONTEXT_REQUEST_ID);
        return value != null ? value.toString() : null;
    }
}
