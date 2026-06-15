package com.example.education.system.ai.service.impl;

import com.example.education.system.ai.model.ChatMessage;
import com.example.education.system.ai.service.ChatHistoryStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis 存储实现 — 聊天历史存入 Redis List。
 * <p>
 * 当 {@code ai.chat-history.storage=redis} 时激活。
 *
 * <h3>数据结构</h3>
 * <pre>{@code
 * Key:   ai:history:{sessionId}   →  List<String>（每条消息一个 JSON 字符串）
 * TTL:   30 分钟（每次新消息刷新）
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.chat-history.storage", havingValue = "redis")
public class RedisChatHistoryStorage implements ChatHistoryStorage {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "ai:history:";
    private static final Duration TTL = Duration.ofMinutes(30);

    @Override
    public List<ChatMessage> loadHistory(String sessionId) {
        List<String> jsonList = stringRedisTemplate.opsForList()
                .range(KEY_PREFIX + sessionId, 0, -1);

        List<ChatMessage> history = new ArrayList<>();
        if (jsonList != null) {
            for (String json : jsonList) {
                try {
                    history.add(objectMapper.readValue(json, ChatMessage.class));
                } catch (JsonProcessingException e) {
                    log.warn("反序列化历史消息失败, sessionId={}, json={}", sessionId, json, e);
                }
            }
        }
        return history;
    }

    @Override
    public void saveMessage(String sessionId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreateTime(new Timestamp(System.currentTimeMillis()));

        String key = KEY_PREFIX + sessionId;
        try {
            String json = objectMapper.writeValueAsString(msg);
            stringRedisTemplate.opsForList().rightPush(key, json);
            // 每次保存刷新 TTL，保持会话活跃期间数据不消失
            stringRedisTemplate.expire(key, TTL);
        } catch (JsonProcessingException e) {
            log.error("序列化消息失败, sessionId={}", sessionId, e);
        }
    }
}
