package com.example.education.system.ai.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.education.system.ai.model.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
