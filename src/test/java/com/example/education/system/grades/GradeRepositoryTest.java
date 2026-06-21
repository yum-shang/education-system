package com.example.education.system.grades;

import com.example.education.system.grades.dto.EnrolledStudentTemplateRow;
import com.example.education.system.grades.model.Grade;
import com.example.education.system.grades.repository.GradeRepository;
import com.example.education.system.grades.service.GradeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GradeRepository 集成测试 — 用 H2 内存数据库测试真实 SQL。
 * <p>
 * 流程：Spring Boot 启动时，自动执行 schema.sql 建表、data.sql 插数据；
 * 测试方法跑在事务中，跑完自动回滚，不会污染数据。
 */
@SpringBootTest
class GradeRepositoryTest {

    @Autowired
    private GradeService gradeService;


}
