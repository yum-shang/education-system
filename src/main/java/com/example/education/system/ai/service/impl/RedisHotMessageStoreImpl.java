package com.example.education.system.ai.service.impl;

import com.example.education.system.ai.model.ChatMessage;
import com.example.education.system.ai.model.ChatSession;
import com.example.education.system.ai.config.AiSummaryConstants;
import com.example.education.system.ai.service.ChatSessionService;
import com.example.education.system.ai.service.RedisHotMessageStore;
import com.example.education.system.ai.support.RedisHotMessageKeys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Redis 热消息存储实现。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = AiSummaryConstants.SUMMARY_ENABLED_PROPERTY, havingValue = AiSummaryConstants.SUMMARY_ENABLED_VALUE)
public class RedisHotMessageStoreImpl implements RedisHotMessageStore {

    private static final Logger log = LoggerFactory.getLogger(RedisHotMessageStoreImpl.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatSessionService chatSessionService;

    @Override
    public void saveMessage(String sessionId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreateTime(new Timestamp(System.currentTimeMillis()));

        try {
            String json = objectMapper.writeValueAsString(msg);
            stringRedisTemplate.opsForList().rightPush(RedisHotMessageKeys.hot(sessionId), json);
            stringRedisTemplate.opsForValue().increment(RedisHotMessageKeys.count(sessionId));
            refreshAllTtls(sessionId);
        } catch (JsonProcessingException e) {
            log.error("热消息序列化失败, sessionId={}", sessionId, e);
        }
    }

    @Override
    public List<ChatMessage> loadAll(String sessionId) {
        return readList(RedisHotMessageKeys.hot(sessionId));
    }

    @Override
    public List<ChatMessage> loadSnapshot(String sessionId) {
        return readList(RedisHotMessageKeys.snapshot(sessionId));
    }

    @Override
    public boolean hasSnapshot(String sessionId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisHotMessageKeys.snapshot(sessionId)));
    }

    @Override
    public void deleteSnapshot(String sessionId) {
        stringRedisTemplate.delete(RedisHotMessageKeys.snapshot(sessionId));
    }

    @Override
    public int getCount(String sessionId) {
        String val = stringRedisTemplate.opsForValue().get(RedisHotMessageKeys.count(sessionId));
        if (val == null || val.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void incrementCount(String sessionId) {
        stringRedisTemplate.opsForValue().increment(RedisHotMessageKeys.count(sessionId));
        refreshAllTtls(sessionId);
    }

    @Override
    public void resetCount(String sessionId) {
        stringRedisTemplate.opsForValue().set(RedisHotMessageKeys.count(sessionId), "0");
        refreshAllTtls(sessionId);
    }

    @Override
    public void syncCountWithHotList(String sessionId) {
        Long size = stringRedisTemplate.opsForList().size(RedisHotMessageKeys.hot(sessionId));
        long count = size == null ? 0 : size;
        stringRedisTemplate.opsForValue().set(RedisHotMessageKeys.count(sessionId), String.valueOf(count));
        refreshAllTtls(sessionId);
    }

    @Override
    public void resetCountAfterCapture(String sessionId) {
        stringRedisTemplate.opsForValue().set(RedisHotMessageKeys.count(sessionId), "0");
        refreshAllTtls(sessionId);
    }

    @Override
    public void refreshTtl(String sessionId) {
        refreshAllTtls(sessionId);
    }

    @Override
    public void refreshAllTtls(String sessionId) {
        Duration ttl = resolveTtl(sessionId);
        if (ttl.isZero() || ttl.isNegative()) {
            log.warn("session TTL 无效, sessionId={}, ttl={}", sessionId, ttl);
            return;
        }
        stringRedisTemplate.expire(RedisHotMessageKeys.hot(sessionId), ttl);
        stringRedisTemplate.expire(RedisHotMessageKeys.count(sessionId), ttl);
        stringRedisTemplate.expire(RedisHotMessageKeys.snapshot(sessionId), ttl);
    }

    @Override
    public void prependToHot(String sessionId, List<ChatMessage> olderFirst) {
        if (olderFirst == null || olderFirst.isEmpty()) {
            return;
        }
        List<ChatMessage> reversed = new ArrayList<>(olderFirst);
        Collections.reverse(reversed);
        String hotKey = RedisHotMessageKeys.hot(sessionId);
        for (ChatMessage msg : reversed) {
            try {
                String json = objectMapper.writeValueAsString(msg);
                stringRedisTemplate.opsForList().leftPush(hotKey, json);
            } catch (JsonProcessingException e) {
                log.warn("prepend 热消息序列化失败, sessionId={}", sessionId, e);
            }
        }
        syncCountWithHotList(sessionId);
    }

    private List<ChatMessage> readList(String redisKey) {
        List<String> jsonList = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
        List<ChatMessage> result = new ArrayList<>();
        if (jsonList == null) {
            return result;
        }
        for (String json : jsonList) {
            try {
                result.add(objectMapper.readValue(json, ChatMessage.class));
            } catch (JsonProcessingException e) {
                log.warn("热消息反序列化失败, key={}, json={}", redisKey, json, e);
            }
        }
        return result;
    }

    private Duration resolveTtl(String sessionId) {
        ChatSession session = chatSessionService.findBySessionId(sessionId).orElse(null);
        if (session == null || session.getExpireAt() == null) {
            return Duration.ofDays(7);
        }
        Instant expireAt = session.getExpireAt().toInstant();
        Duration ttl = Duration.between(Instant.now(), expireAt);
        return ttl.isNegative() ? Duration.ZERO : ttl;
    }
}
