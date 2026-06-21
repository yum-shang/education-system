package com.example.education.system.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.education.system.ai.model.ChatMessage;
import com.example.education.system.ai.repository.ChatMessageMapper;
import com.example.education.system.ai.config.LegacyChatHistoryConditions;
import com.example.education.system.ai.service.ChatHistoryStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

/**
 * MySQL 存储实现 — 聊天历史存入 {@code ai_messages} 表。
 * <p>
 * 当 {@code ai.chat-history.storage=mysql} 或未配置时激活（默认）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Conditional(LegacyChatHistoryConditions.Mysql.class)
public class MySqlChatHistoryStorage implements ChatHistoryStorage {

    private final ChatMessageMapper chatMessageMapper;

    @Override
    public List<ChatMessage> loadHistory(String sessionId) {
        return loadHistory(sessionId, Integer.MAX_VALUE);
    }

    @Override
    public List<ChatMessage> loadHistory(String sessionId, int maxMessages) {
        if (maxMessages <= 0) {
            return List.of();
        }
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId);
        if (maxMessages < Integer.MAX_VALUE) {
            // 先取最新 N 条再反转，保证返回升序且只保留最近上下文
            wrapper.orderByDesc(ChatMessage::getCreateTime)
                    .last("LIMIT " + maxMessages);
            List<ChatMessage> recent = chatMessageMapper.selectList(wrapper);
            recent = new java.util.ArrayList<>(recent);
            java.util.Collections.reverse(recent);
            return recent;
        }
        return chatMessageMapper.selectList(
                wrapper.orderByAsc(ChatMessage::getCreateTime)
        );
    }

    @Override
    public void saveMessage(String sessionId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreateTime(new Timestamp(System.currentTimeMillis()));
        chatMessageMapper.insert(msg);
    }
}
