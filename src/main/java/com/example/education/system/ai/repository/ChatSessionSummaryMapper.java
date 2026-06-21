package com.example.education.system.ai.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.education.system.ai.model.ChatSessionSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionSummaryMapper extends BaseMapper<ChatSessionSummary> {
}
