package com.example.education.system.users.controller;

import com.example.education.system.users.dto.UserListResponse;
import com.example.education.system.users.dto.ProfileResponse;
import com.example.education.system.users.service.UserService;
import com.example.education.system.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HttpServletRequest request;

    // 从JWT中获取用户ID
    private Integer getUserIdFromToken() {
        String token = request.getHeader("Authorization").substring(7);
        return jwtService.getUserIdFromToken(token);
    }

    // 从JWT中获取用户角色
    private String getRoleFromToken() {
        String token = request.getHeader("Authorization").substring(7);
        return jwtService.getRoleFromToken(token);
    }

    @GetMapping
    public UserListResponse getUserList(
            @RequestParam(required = false) String role,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        String currentRole = getRoleFromToken();
        if (!"admin".equals(currentRole)) {
            UserListResponse response = new UserListResponse();
            response.setCode(403);
            response.setMessage("权限不足，只有管理员可以获取用户列表");
            return response;
        }
        if ("admin".equals(role)) {
            UserListResponse response = new UserListResponse();
            response.setCode(400);
            response.setMessage("不支持查询管理员列表");
            return response;
        }
        return userService.getUserList(role, page, pageSize);
    }

    @PutMapping("/{userId}/password")
    public UserListResponse updateUserPassword(
            @PathVariable Integer userId,
            @RequestParam String newPassword) {
        // 检查权限，只有管理员或用户自己可以修改密码
        Integer currentUserId = getUserIdFromToken();
        String currentRole = getRoleFromToken();
        
        if (!"admin".equals(currentRole) && !currentUserId.equals(userId)) {
            UserListResponse response = new UserListResponse();
            response.setCode(403);
            response.setMessage("权限不足，只能修改自己的密码");
            return response;
        }
        
        userService.updateUserPassword(userId, newPassword);
        UserListResponse response = new UserListResponse();
        response.setCode(200);
        response.setMessage("密码修改成功");
        return response;
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile() {
        // 从JWT中获取用户ID
        Integer userId = getUserIdFromToken();
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(@RequestBody ProfileResponse.Data profileData) {
        // 从JWT中获取用户ID
        Integer userId = getUserIdFromToken();
        userService.updateProfile(userId, profileData);
        ProfileResponse response = new ProfileResponse();
        response.setCode(200);
        response.setMessage("信息修改成功");
        return response;
    }

    @GetMapping("/{userId}")
    public ProfileResponse getUserById(@PathVariable Integer userId) {
        // 检查权限，管理员可以查看所有用户，普通用户只能查看自己
        Integer currentUserId = getUserIdFromToken();
        String currentRole = getRoleFromToken();
        
        if (!"admin".equals(currentRole) && !currentUserId.equals(userId)) {
            ProfileResponse response = new ProfileResponse();
            response.setCode(403);
            response.setMessage("权限不足，只能查看自己的信息");
            return response;
        }
        
        return userService.getProfile(userId);
    }
}