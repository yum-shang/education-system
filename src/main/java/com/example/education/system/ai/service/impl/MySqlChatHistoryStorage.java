package com.example.education.system.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.education.system.ai.model.ChatMessage;
import com.example.education.system.ai.repository.ChatMessageMapper;
import com.example.education.system.ai.service.ChatHistoryStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "ai.chat-history.storage", havingValue = "mysql", matchIfMissing = true)
public class MySqlChatHistoryStorage implements ChatHistoryStorage {

    private final ChatMessageMapper chatMessageMapper;

    @Override
    public List<ChatMessage> loadHistory(String sessionId) {
        return chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreateTime)
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
