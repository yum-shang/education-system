package com.example.education.system.courses.tool;

import com.example.education.system.courses.model.Course;
import com.example.education.system.courses.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 课程查询工具 — 供 AI Function Calling 调用。
 *
 * <h3>工作原理</h3>
 * <ol>
 *   <li>Spring AI 自动扫描 {@code @Tool} 注解的方法，生成工具定义发给 DeepSeek</li>
 *   <li>DeepSeek 判断需要调用工具时，返回 function_call 请求</li>
 *   <li>Spring AI 拦截 function_call，反射调用对应方法，结果回传 DeepSeek</li>
 *   <li>DeepSeek 基于真实数据组织回答</li>
 * </ol>
 *
 * <h3>1003 事件传递机制</h3>
 * <p>
 * 由于工具方法运行在 WebClient 的 I/O 线程上（非 Controller 线程），
 * ThreadLocal 无法跨线程传递 requestId。
 * <p>
 * 替代方案：使用 {@code activeRequests} 注册表。
 * <ol>
 *   <li>ChatServiceImpl 在 AI 流开始前调用 {@link #registerActiveRequest(String)} 注册 requestId</li>
 *   <li>工具方法被调用时，遍历所有活跃 requestId，往每个下面写入课程数据</li>
 *   <li>ChatServiceImpl 在流结束后通过 {@link #takeCourseEvents(String)} 取出并清理</li>
 * </ol>
 * 在低并发场景下（学生项目），数据不会串台。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseQueryTools {

    private final CourseRepository courseRepository;

    /**
     * 活跃请求注册表：requestId → 注册时间。
     * 工具方法被 AI 调用时，往所有活跃 requestId 写入课程数据。
     */
    private final ConcurrentHashMap<String, Instant> activeRequests = new ConcurrentHashMap<>();

    /**
     * 课程事件缓存：requestId → List&lt;课程数据&gt;。
     */
    private final ConcurrentHashMap<String, List<Map<String, Object>>> courseEventCache = new ConcurrentHashMap<>();

    /**
     * 注册活跃请求。ChatServiceImpl 在 AI 流开始前调用。
     */
    public void registerActiveRequest(String requestId) {
        activeRequests.put(requestId, Instant.now());
    }

    /**
     * 取出并移除指定 requestId 的课程事件，同时注销活跃请求。
     * ChatServiceImpl 在 AI 流结束后调用。
     */
    public List<Map<String, Object>> takeCourseEvents(String requestId) {
        activeRequests.remove(requestId);
        return courseEventCache.remove(requestId);
    }

    /**
     * 按课程 ID 精确查询课程信息。
     * 对应系统提示词技能1第6条："推荐课程必须通过 findCourseById 查询后，才能返回数据"
     */
    @Tool(description = "根据课程ID精确查询课程的详细信息，包括课程名称、学分、课程代码和课程描述")
    public Course findCourseById(
            @ToolParam(description = "课程ID，整数") Integer courseId) {

        Course course = courseRepository.findCourseById(courseId);
        log.debug("AI 调用 findCourseById, courseId={}, activeRequests={}", courseId, activeRequests.keySet());
        if (course != null) {
            Map<String, Object> courseData = new LinkedHashMap<>();
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", String.valueOf(course.getCourseId()));
            info.put("name", course.getCourseName());
            info.put("code", course.getCourseCode());
            info.put("credit", course.getCredit());
            info.put("description", course.getDescription());
            courseData.put("courseInfo_" + course.getCourseId(), info);

            // 往所有活跃请求写入课程数据（不再依赖 ThreadLocal）
            for (String requestId : activeRequests.keySet()) {
                courseEventCache.computeIfAbsent(requestId, k -> new ArrayList<>()).add(courseData);
            }
            log.debug("1003课程数据已写入 {} 个活跃请求", activeRequests.size());
        }
        return course;
    }

    /**
     * 搜索课程（可选关键词）。
     * 不传 keyword 时返回数据库中全部课程，方便 AI 了解完整的课程库再做推荐。
     * 传入 keyword 时按课程名称或课程代码模糊匹配。
     */
    @Tool(description = "搜索课程。不传参数时返回全部课程列表；传入关键词时按课程名称或代码模糊匹配（最多50条）")
    public List<Course> findCourses(
            @ToolParam(description = "搜索关键词（可选）。留空则返回全部课程", required = false) String keyword) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        return courseRepository.findCourses(kw, kw, 0, 50);
    }
}
