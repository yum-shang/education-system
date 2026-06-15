package com.example.education.system.ai.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.education.system.ai.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
