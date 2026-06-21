package com.example.education.system.ai.service;

import com.example.education.system.ai.model.ChatMessage;

import java.util.List;

/**
 * Redis 热消息存储：存放尚未被滚动摘要的原始对话。
 * <p>
 * TTL 与 {@code ai_sessions.expire_at} 对齐，由 {@link #refreshTtl(String)} 刷新。
 */
public interface RedisHotMessageStore {

    void saveMessage(String sessionId, String role, String content);

    /** 当前活跃热消息（升序） */
    List<ChatMessage> loadAll(String sessionId);

    /** 摘要快照中的消息（升序），摘要进行中用于上下文组装 */
    List<ChatMessage> loadSnapshot(String sessionId);

    boolean hasSnapshot(String sessionId);

    void deleteSnapshot(String sessionId);

    int getCount(String sessionId);

    void incrementCount(String sessionId);

    void resetCount(String sessionId);

    /** 将 count 设为当前 hot List 的实际长度 */
    void syncCountWithHotList(String sessionId);

    /** 快照捕获后 count 归零（热消息已 RENAME 到 snapshot） */
    void resetCountAfterCapture(String sessionId);

    void refreshTtl(String sessionId);

    /** 刷新 hot、count、snapshot 的 TTL */
    void refreshAllTtls(String sessionId);

    /**
     * 将 olderFirst 中的消息按顺序 prepend 到 hot List 头部（用于失败回滚）。
     */
    void prependToHot(String sessionId, List<ChatMessage> olderFirst);
}
