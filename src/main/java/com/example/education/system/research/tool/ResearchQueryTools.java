package com.example.education.system.research.tool;

import com.example.education.system.courses.dto.EnrollmentInfo;
import com.example.education.system.courses.repository.CourseEnrollmentRepository;
import com.example.education.system.research.model.ResearchProject;
import com.example.education.system.research.repository.ResearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 科研项目查询工具 — 供 AI Function Calling 调用。
 *
 * <p>查询已发布且未结束的科研项目，并根据学生选课标签做智能推荐。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResearchQueryTools {

    public static final String CONTEXT_REQUEST_ID = "requestId";

    private final ResearchRepository researchRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    private final ConcurrentHashMap<String, List<Map<String, Object>>> researchEventCache = new ConcurrentHashMap<>();

    /**
     * 取出并移除指定 requestId 的科研项目推荐事件。
     */
    public List<Map<String, Object>> takeResearchEvents(String requestId) {
        return researchEventCache.remove(requestId);
    }

    /**
     * 查询已发布的、招募中的、还没有结束的科研项目。
     */
    @Tool(description = "查询已发布且招募中的科研项目列表，可按关键词搜索项目名称、描述或标签")
    public List<ResearchProject> findResearchProjects(
            @ToolParam(description = "搜索关键词（可选），匹配项目名称、描述或标签", required = false) String keyword,
            ToolContext toolContext) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        List<ResearchProject> projects = researchRepository.findProjects("recruiting", null, kw, 0, 50);
        log.debug("AI 调用 findResearchProjects, keyword={}, 结果数={}", kw, projects.size());
        return projects;
    }

    /**
     * 根据学生选修课程，匹配科研项目标签，推荐最相关的项目。
     */
    @Tool(description = "根据学生ID查询其选修课程，再匹配科研项目标签，推荐最相关的科研项目")
    public String recommendProjects(
            @ToolParam(description = "学生ID，整数") Integer studentId,
            ToolContext toolContext) {

        // 1. 查学生选了哪些课
        List<EnrollmentInfo> enrollments = enrollmentRepository.findEnrollmentInfoByStudentId(
                studentId, null, null, 0, 100);

        Set<String> courseNames = enrollments.stream()
                .map(EnrollmentInfo::getCourseName)
                .collect(Collectors.toSet());

        // 2. 查所有招募中的科研项目
        List<ResearchProject> projects = researchRepository.findProjects("recruiting", null, null, 0, 100);

        // 3. 按标签与课程名的匹配度排序
        List<ResearchProject> ranked = projects.stream()
                .sorted((a, b) -> Integer.compare(
                        matchScore(b.getTags(), courseNames),
                        matchScore(a.getTags(), courseNames)))
                .limit(10)
                .collect(Collectors.toList());

        // 5. 返回文本摘要给 AI 组织回复语言
        if (ranked.isEmpty()) {
            return "暂未找到与学生选课方向匹配的科研项目";
        }
        return ranked.stream()
                .map(p -> p.getProjectName() + "（标签：" + p.getTags() + "）")
                .collect(Collectors.joining("；"));
    }

    /**
     * 标签与课程名的交集打分。
     */
    private int matchScore(String tags, Set<String> courseNames) {
        if (tags == null || tags.isBlank()) return 0;
        int score = 0;
        for (String tag : tags.split(",")) {
            for (String name : courseNames) {
                if (tag.trim().equalsIgnoreCase(name.trim())) score++;
            }
        }
        return score;
    }

    private String resolveRequestId(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) return null;
        Object value = toolContext.getContext().get(CONTEXT_REQUEST_ID);
        return value != null ? value.toString() : null;
    }
}
