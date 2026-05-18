package com.example.education.system.auth.service;

import com.example.education.system.auth.dto.RegisterRequest;
import com.example.education.system.auth.dto.LoginRequest;
import com.example.education.system.auth.dto.AuthResponse;
import com.example.education.system.auth.model.User;
import com.example.education.system.auth.repository.UserRepository;
import com.example.education.system.users.model.Teacher;
import com.example.education.system.users.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 用户认证服务
 * 
 * 负责处理用户注册、登录等认证相关业务逻辑，包括：
 * - 用户注册（教师/学生）
 * - 用户登录验证
 * - JWT令牌生成
 * - 用户信息初始化
 */
@Service
@Slf4j
public class AuthService {

    @Autowired
    @Qualifier("authUserRepository")
    private UserRepository authUserRepository;

    @Autowired
    @Qualifier("userRepository")
    private com.example.education.system.users.repository.UserRepository usersUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (authUserRepository.findByUsername(request.getUsername()) != null) {
            AuthResponse response = new AuthResponse();
            response.setCode(400);
            response.setMessage("用户名已存在");
            return response;
        }

        // 检查邮箱是否已存在
        if (authUserRepository.findByEmail(request.getEmail()) != null) {
            AuthResponse response = new AuthResponse();
            response.setCode(400);
            response.setMessage("邮箱已存在");
            return response;
        }

        // 检查手机号是否已存在
        if (authUserRepository.findByPhone(request.getPhone()) != null) {
            AuthResponse response = new AuthResponse();
            response.setCode(400);
            response.setMessage("手机号已存在");
            return response;
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setCreatedAt(new Timestamp(new Date().getTime()));
        user.setUpdatedAt(new Timestamp(new Date().getTime()));

        authUserRepository.insert(user);

        // 根据角色创建详细信息
        if ("teacher".equals(request.getRole())) {
            // 创建教师记录
            Teacher teacher = new Teacher();
            teacher.setTeacherId(user.getUserId());
            teacher.setName(request.getName());
            teacher.setTitle(request.getTitle());
            teacher.setDepartment(request.getDepartment());
            usersUserRepository.insertTeacher(teacher);
        } else if ("student".equals(request.getRole())) {
            // 创建学生记录
            Student student = new Student();
            student.setStudentId(user.getUserId());
            student.setName(request.getName());
            student.setStudentNumber(request.getStudentNumber());
            student.setMajor(request.getMajor());
            student.setGrade(request.getGrade());
            student.setClazz(request.getClazz());
            usersUserRepository.insertStudent(student);
        }

        // 生成JWT令牌
        String token = jwtService.generateToken(user.getUserId(), user.getRole());

        // 构建响应
        AuthResponse response = new AuthResponse();
        response.setCode(200);
        response.setMessage("注册成功");

        AuthResponse.Data data = new AuthResponse.Data();
        data.setUserId(user.getUserId());
        data.setUsername(user.getUsername());
        data.setRole(user.getRole());
        data.setToken(token);

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRole(user.getRole());
        userInfo.setAvatarId(user.getAvatarId());

        data.setUser(userInfo);
        response.setData(data);

        return response;
    }

    public AuthResponse login(LoginRequest request) {
        // 查找用户
        User user = authUserRepository.findByUsername(request.getUsername());
        if (user == null) {
            AuthResponse response = new AuthResponse();
            response.setCode(400);
            response.setMessage("用户名或密码错误");
            return response;
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            AuthResponse response = new AuthResponse();
            response.setCode(400);
            response.setMessage("用户名或密码错误");
            return response;
        }

        // 生成JWT令牌
        String token = jwtService.generateToken(user.getUserId(), user.getRole());

        // 构建响应
        AuthResponse response = new AuthResponse();
        response.setCode(200);
        response.setMessage("登录成功");

        AuthResponse.Data data = new AuthResponse.Data();
        data.setUserId(user.getUserId());
        data.setUsername(user.getUsername());
        data.setRole(user.getRole());
        data.setToken(token);

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRole(user.getRole());
        userInfo.setAvatarId(user.getAvatarId());

        data.setUser(userInfo);
        response.setData(data);

        return response;
    }
}