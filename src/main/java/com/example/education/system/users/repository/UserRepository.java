package com.example.education.system.users.repository;

import com.example.education.system.auth.model.User;
import com.example.education.system.users.model.Teacher;
import com.example.education.system.users.model.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userRepository")
@Mapper
public interface UserRepository {
    // 用户相关操作
    List<User> findUsers(@Param("role") String role, @Param("offset") Integer offset, @Param("limit") Integer limit);
    Integer countUsers(@Param("role") String role);
    User findUserById(@Param("userId") Integer userId);
    User findByPhone(@Param("phone") String phone);
    User findByEmail(@Param("email") String email);
    void updateUser(User user);
    void deleteUser(@Param("userId") Integer userId);

    // 学生列表查询（支持关键词、院系、年级筛选）
    List<Student> findStudents(@Param("keyword") String keyword, @Param("department") String department,
                               @Param("grade") String grade, @Param("offset") Integer offset, @Param("limit") Integer limit);
    Integer countStudents(@Param("keyword") String keyword, @Param("department") String department, @Param("grade") String grade);

    // 教师相关操作
    Teacher findTeacherById(@Param("teacherId") Integer teacherId);
    void insertTeacher(Teacher teacher);
    void updateTeacher(Teacher teacher);

    // 学生相关操作
    Student findStudentById(@Param("studentId") Integer studentId);
    void insertStudent(Student student);
    void updateStudent(Student student);
}