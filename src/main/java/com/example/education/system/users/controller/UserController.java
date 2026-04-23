package com.example.education.system.users.controller;

import com.example.education.system.users.dto.UserListResponse;
import com.example.education.system.users.dto.ProfileResponse;
import com.example.education.system.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public UserListResponse getUserList(
            @RequestParam(required = false) String role,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        return userService.getUserList(role, page, pageSize);
    }

    @PutMapping("/{userId}/password")
    public UserListResponse updateUserPassword(
            @PathVariable Integer userId,
            @RequestParam String newPassword) {
        userService.updateUserPassword(userId, newPassword);
        UserListResponse response = new UserListResponse();
        response.setCode(200);
        response.setMessage("密码修改成功");
        return response;
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile() {
        // 从JWT中获取用户ID
        Integer userId = 1; // 简化处理，实际应该从JWT令牌中获取
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(@RequestBody ProfileResponse.Data profileData) {
        // 从JWT中获取用户ID
        Integer userId = 1; // 简化处理，实际应该从JWT令牌中获取
        userService.updateProfile(userId, profileData);
        ProfileResponse response = new ProfileResponse();
        response.setCode(200);
        response.setMessage("信息修改成功");
        return response;
    }
}