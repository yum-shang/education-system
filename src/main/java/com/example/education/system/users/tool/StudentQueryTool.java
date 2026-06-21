package com.example.education.system.users.tool;

import com.example.education.system.users.model.Student;
import com.example.education.system.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 学生查询工具 — 供 AI Function Calling 调用。
 */
@Slf4j
@Component
public class StudentQueryTool {

    private final UserRepository userRepository;

    public StudentQueryTool(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Tool(description = "根据学号查询学生信息，返回学生的姓名、学号、专业、学院、年级")
    public List<String> findStudentById(
            @ToolParam(description = "学生ID，整数") Integer studentId,
            @ToolParam(description = "学生姓名（可选）", required = false) String name,
            @ToolParam(description = "专业", required = true) String major,
            @ToolParam(description = "学院", required = true) String department,
            ToolContext toolContext) {

        Student student = userRepository.findStudentById(studentId);
        log.debug("AI 调用 findStudentById, studentId={}", studentId);

        if (student == null) {
            return List.of();
        }
        return List.of(
                "姓名:" + student.getName()
                        + ", 学号:" + student.getStudentNumber()
                        + ", 专业:" + student.getMajor()
                        + ", 学院:" + student.getDepartment()
                        + ", 年级:" + student.getGrade()
        );
    }
}
