package com.example.education.system.users.service;

import com.example.education.system.users.dto.UserListResponse;
import com.example.education.system.users.dto.ProfileResponse;
import com.example.education.system.auth.model.User;
import com.example.education.system.users.model.Teacher;
import com.example.education.system.users.model.Student;
import com.example.education.system.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserListResponse getUserList(String role, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<User> users = userRepository.findUsers(role, offset, pageSize);
        Integer total = userRepository.countUsers(role);

        UserListResponse response = new UserListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        UserListResponse.Data data = new UserListResponse.Data();
        List<UserListResponse.UserInfo> userInfos = new ArrayList<>();

        for (User user : users) {
            UserListResponse.UserInfo info = new UserListResponse.UserInfo();
            info.setUserId(user.getUserId());
            info.setUsername(user.getUsername());
            info.setEmail(user.getEmail());
            info.setPhone(user.getPhone());
            info.setRole(user.getRole());
            info.setAvatarId(user.getAvatarId());
            info.setCreatedAt(user.getCreatedAt().toString());
            userInfos.add(info);
        }

        data.setList(userInfos);
        data.setTotal(total);
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    public void updateUserPassword(Integer userId, String newPassword) {
        User user = userRepository.findUserById(userId);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.updateUser(user);
        }
    }

    public ProfileResponse getProfile(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            ProfileResponse response = new ProfileResponse();
            response.setCode(404);
            response.setMessage("用户不存在");
            return response;
        }

        ProfileResponse response = new ProfileResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        ProfileResponse.Data data = new ProfileResponse.Data();
        data.setUserId(user.getUserId());
        data.setUsername(user.getUsername());
        data.setEmail(user.getEmail());
        data.setPhone(user.getPhone());
        data.setRole(user.getRole());
        data.setAvatarId(user.getAvatarId());

        // 根据用户角色查询详细信息
        if ("teacher".equals(user.getRole())) {
            Teacher teacher = userRepository.findTeacherById(userId);
            if (teacher != null) {
                data.setName(teacher.getName());
                data.setTitle(teacher.getTitle());
                data.setDepartment(teacher.getDepartment());
                data.setBio(teacher.getBio());
            }
        } else if ("student".equals(user.getRole())) {
            Student student = userRepository.findStudentById(userId);
            if (student != null) {
                data.setName(student.getName());
                data.setStudentNumber(student.getStudentNumber());
                data.setMajor(student.getMajor());
                data.setGrade(student.getGrade());
                data.setClazz(student.getClazz());
            }
        }

        response.setData(data);
        return response;
    }

    public void updateProfile(Integer userId, ProfileResponse.Data profileData) {
        // 更新用户基本信息
        User user = userRepository.findUserById(userId);
        if (user != null) {
            user.setEmail(profileData.getEmail());
            user.setPhone(profileData.getPhone());
            user.setAvatarId(profileData.getAvatarId());
            userRepository.updateUser(user);

            // 根据用户角色更新详细信息
            if ("teacher".equals(user.getRole())) {
                Teacher teacher = userRepository.findTeacherById(userId);
                if (teacher != null) {
                    teacher.setName(profileData.getName());
                    teacher.setTitle(profileData.getTitle());
                    teacher.setDepartment(profileData.getDepartment());
                    teacher.setBio(profileData.getBio());
                    userRepository.updateTeacher(teacher);
                } else {
                    // 如果教师信息不存在，创建新的教师记录
                    Teacher newTeacher = new Teacher();
                    newTeacher.setTeacherId(userId);
                    newTeacher.setName(profileData.getName());
                    newTeacher.setTitle(profileData.getTitle());
                    newTeacher.setDepartment(profileData.getDepartment());
                    newTeacher.setBio(profileData.getBio());
                    userRepository.insertTeacher(newTeacher);
                }
            } else if ("student".equals(user.getRole())) {
                Student student = userRepository.findStudentById(userId);
                if (student != null) {
                    student.setName(profileData.getName());
                    student.setStudentNumber(profileData.getStudentNumber());
                    student.setMajor(profileData.getMajor());
                    student.setGrade(profileData.getGrade());
                    student.setClazz(profileData.getClazz());
                    userRepository.updateStudent(student);
                } else {
                    // 如果学生信息不存在，创建新的学生记录
                    Student newStudent = new Student();
                    newStudent.setStudentId(userId);
                    newStudent.setName(profileData.getName());
                    newStudent.setStudentNumber(profileData.getStudentNumber());
                    newStudent.setMajor(profileData.getMajor());
                    newStudent.setGrade(profileData.getGrade());
                    newStudent.setClazz(profileData.getClazz());
                    userRepository.insertStudent(newStudent);
                }
            }
        }
    }
}