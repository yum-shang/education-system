package com.example.education.system.auth.repository;

import com.example.education.system.auth.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository("authUserRepository")
@Mapper
public interface UserRepository {
    User findByUsername(@Param("username") String username);
    User findByEmail(@Param("email") String email);
    User findByPhone(@Param("phone") String phone);
    void insert(User user);
    void update(User user);
    void updatePassword(@Param("userId") Integer userId, @Param("password") String password);
}