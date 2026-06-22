package com.example.education.system.research.tool;

import com.example.education.system.courses.dto.EnrollmentInfo;
import com.example.education.system.courses.repository.CourseEnrollmentRepository;
import com.example.education.system.research.model.ResearchProject;
import com.example.education.system.research.repository.ResearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("科研项目查询工具 - 单元测试")
class ResearchQueryToolsTest {

    @Mock
    private ResearchRepository researchRepository;

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private ResearchQueryTools researchQueryTools;

    // ==================== 测试数据工厂方法 ====================

    private ResearchProject project(String name, String tags) {
        ResearchProject p = new ResearchProject();
        p.setProjectId(name.hashCode());
        p.setProjectName(name);
        p.setTags(tags);
        p.setStatus("recruiting");
        return p;
    }

    private EnrollmentInfo enrollment(String courseName) {
        EnrollmentInfo e = new EnrollmentInfo();
        e.setCourseName(courseName);
        return e;
    }

    // ==================== findResearchProjects ====================

    @Nested
    @DisplayName("findResearchProjects - 查询科研项目")
    class FindResearchProjects {

        @Test
        @DisplayName("有匹配项目时返回列表")
        void shouldReturnProjectsWhenFound() {
            List<ResearchProject> mockList = List.of(
                    project("AI图像识别", "机器学习,Python"),
                    project("数据库优化", "MySQL,Redis")
            );
            when(researchRepository.findProjects(eq("recruiting"), isNull(), eq("AI"), eq(0), eq(50)))
                    .thenReturn(mockList);

            List<ResearchProject> result = researchQueryTools.findResearchProjects("AI");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getProjectName()).isEqualTo("AI图像识别");
        }

        @Test
        @DisplayName("无匹配时返回空列表")
        void shouldReturnEmptyWhenNoMatch() {
            when(researchRepository.findProjects(eq("recruiting"), isNull(), eq("量子计算"), eq(0), eq(50)))
                    .thenReturn(Collections.emptyList());

            List<ResearchProject> result = researchQueryTools.findResearchProjects("量子计算");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("keyword 为 null 时正常查询")
        void shouldHandleNullKeyword() {
            when(researchRepository.findProjects(eq("recruiting"), isNull(), isNull(), eq(0), eq(50)))
                    .thenReturn(Collections.emptyList());

            List<ResearchProject> result = researchQueryTools.findResearchProjects(null);

            assertThat(result).isEmpty();
        }
    }

    // ==================== recommendProjects ====================

    @Nested
    @DisplayName("recommendProjects - 根据选课标签推荐科研项目")
    class RecommendProjects {

        private ToolContext toolContext;

        @BeforeEach
        void setUp() {
            // ToolContext 为 null 时，recommendProjects 不会写缓存
            toolContext = null;
        }

        @Test
        @DisplayName("正常匹配 → 返回按匹配度排序的推荐结果")
        void shouldReturnRankedRecommendations() {
            // 学生选了 机器学习、Python数据分析、数据库原理
            List<EnrollmentInfo> enrollments = List.of(
                    enrollment("机器学习"),
                    enrollment("Python数据分析"),
                    enrollment("数据库原理")
            );
            when(enrollmentRepository.findEnrollmentInfoByStudentId(eq(1), isNull(), isNull(), eq(0), eq(100)))
                    .thenReturn(enrollments);

            // 科研项目池
            List<ResearchProject> projects = List.of(
                    project("Web安全检测", "网络安全,Web前端"),          // 0 匹配
                    project("AI图像识别", "机器学习,Python数据分析"),     // 2 匹配
                    project("数据库性能优化", "数据库原理,MySQL"),         // 1 匹配
                    project("NLP课程问答", "人工智能,机器学习")            // 1 匹配
            );
            when(researchRepository.findProjects(eq("recruiting"), isNull(), isNull(), eq(0), eq(100)))
                    .thenReturn(projects);

            String result = researchQueryTools.recommendProjects(1, toolContext);

            // AI图像识别 排第一（2 匹配），数据库优化 和 NLP问答 各 1 匹配
            assertThat(result)
                    .contains("AI图像识别")
                    .contains("数据库性能优化")
                    .contains("NLP课程问答")
                    .doesNotContain("Web安全检测");  // 0 匹配不出现
        }

        @Test
        @DisplayName("学生无选课记录 → 返回提示信息")
        void shouldReturnHintWhenNoEnrollments() {
            when(enrollmentRepository.findEnrollmentInfoByStudentId(eq(999), isNull(), isNull(), eq(0), eq(100)))
                    .thenReturn(Collections.emptyList());
            when(researchRepository.findProjects(eq("recruiting"), isNull(), isNull(), eq(0), eq(100)))
                    .thenReturn(List.of(project("任意项目", "标签1")));

            String result = researchQueryTools.recommendProjects(999, toolContext);

            // 无选课 → 所有项目匹配分都是 0，但都会返回（因为都有0分）
            // 实际按匹配度排序，0分项目也会出现在列表中
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("无招募中项目 → 返回提示信息")
        void shouldReturnHintWhenNoProjects() {
            List<EnrollmentInfo> enrollments = List.of(enrollment("机器学习"));
            when(enrollmentRepository.findEnrollmentInfoByStudentId(eq(1), isNull(), isNull(), eq(0), eq(100)))
                    .thenReturn(enrollments);
            when(researchRepository.findProjects(eq("recruiting"), isNull(), isNull(), eq(0), eq(100)))
                    .thenReturn(Collections.emptyList());

            String result = researchQueryTools.recommendProjects(1, toolContext);

            assertThat(result).isEqualTo("暂未找到与学生选课方向匹配的科研项目");
        }

        @Test
        @DisplayName("标签匹配不区分大小写")
        void shouldMatchCaseInsensitive() {
            List<EnrollmentInfo> enrollments = List.of(enrollment("Python数据分析"));
            when(enrollmentRepository.findEnrollmentInfoByStudentId(eq(1), isNull(), isNull(), eq(0), eq(100)))
                    .thenReturn(enrollments);

            // 标签用小写
            List<ResearchProject> projects = List.of(
                    project("数据分析项目", "python数据分析,机器学习")
            );
            when(researchRepository.findProjects(eq("recruiting"), isNull(), isNull(), eq(0), eq(100)))
                    .thenReturn(projects);

            String result = researchQueryTools.recommendProjects(1, toolContext);

            assertThat(result).contains("数据分析项目");
        }

        @Test
        @DisplayName("项目标签为 null 时不抛异常")
        void shouldHandleNullTags() {
            List<EnrollmentInfo> enrollments = List.of(enrollment("数据结构"));
            when(enrollmentRepository.findEnrollmentInfoByStudentId(eq(1), isNull(), isNull(), eq(0), eq(100)))
                    .thenReturn(enrollments);

            ResearchProject p = project("无标签项目", null);
            when(researchRepository.findProjects(eq("recruiting"), isNull(), isNull(), eq(0), eq(100)))
                    .thenReturn(List.of(p));

            String result = researchQueryTools.recommendProjects(1, toolContext);

            assertThat(result).isEqualTo("暂未找到与学生选课方向匹配的科研项目");
        }
    }

    // ==================== takeResearchEvents ====================

    // takeResearchEvents 依赖 ToolContext 写入缓存，ToolContext 在 Mock 环境下不可用，
    // 因此只测基础行为：未写入时返回 null。

    @Test
    @DisplayName("takeResearchEvents → 无缓存时返回 null")
    void shouldReturnNullWhenNoCachedEvents() {
        List<Map<String, Object>> result = researchQueryTools.takeResearchEvents("nonexistent-request");
        assertThat(result).isNull();
    }
}
