package com.example.education.system.grades;

import com.example.education.system.grades.dto.GradeListResponse;
import com.example.education.system.grades.repository.GradeRepository;
import com.example.education.system.grades.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * GradeService 单元测试 — 只测 Service 自身逻辑，数据库用 Mock 替代。
 * <p>
 * 原理：@Mock 创建一个假的 GradeRepository，不会真正连数据库；
 * {@code @InjectMocks} 把 Mock 的 Repository 注入到 Service 中。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("成绩服务 - 单元测试")
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private GradeService gradeService;

    // ==================== getStudentGrades ====================

    @Test
    @DisplayName("查询学生成绩 → 有数据时返回分页结果")
    void shouldReturnPaginatedGradesWhenDataExists() {
        // 1. 准备 Mock 数据
        GradeListResponse.GradeInfo info = new GradeListResponse.GradeInfo();
        info.setGradeId(1);
        info.setStudentName("张三");
        info.setCourseName("数据结构");
        info.setScore(88.5);

        List<GradeListResponse.GradeInfo> mockList = List.of(info);

        // 2. 设定 Mock 行为：当调用这两个方法时，返回我们准备好的假数据
        when(gradeRepository.findGradeInfoByStudentId(eq(1), any(), any(), any(), any()))
                .thenReturn(mockList);
        when(gradeRepository.countGradeInfoByStudentId(eq(1), any(), any()))
                .thenReturn(1);

        // 3. 调用被测试的方法
        GradeListResponse response = gradeService.getStudentGrades(1, null, null, 1, 10);

        // 4. 断言：验证返回结果是否正确
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("获取成功");
        assertThat(response.getData().getTotal()).isEqualTo(1);
        assertThat(response.getData().getPage()).isEqualTo(1);
        assertThat(response.getData().getPageSize()).isEqualTo(10);
        assertThat(response.getData().getList()).hasSize(1);
    }

    @Test
    @DisplayName("查询学生成绩 → 无数据时返回空列表")
    void shouldReturnEmptyListWhenNoData() {
        when(gradeRepository.findGradeInfoByStudentId(eq(2), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(gradeRepository.countGradeInfoByStudentId(eq(2), any(), any()))
                .thenReturn(0);

        GradeListResponse response = gradeService.getStudentGrades(2, null, null, 1, 10);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData().getTotal()).isEqualTo(0);
        assertThat(response.getData().getList()).isEmpty();
    }

    // ==================== createGrade ====================

    @Test
    @DisplayName("录入成绩 → teacherId 为 null 时抛异常")
    void shouldThrowExceptionWhenTeacherIdIsNull() {
        try {
            gradeService.createGrade(null, null);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("教师ID不能为空");
        }
    }
}
