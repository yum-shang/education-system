package com.example.education.system.ai.service;

import com.example.education.system.ai.model.ChatMessage;

import java.util.List;

/**
 * AI 聊天历史存储策略接口。
 *
 * <h3>设计思路</h3>
 * <ul>
 *   <li>定义统一的存储契约：加载历史 + 保存消息</li>
 *   <li>MySQL 和 Redis 各写一个实现，通过配置文件 {@code ai.chat-history.storage} 切换</li>
 *   <li>Spring Boot 的 {@code @ConditionalOnProperty} 按配置激活对应的 Bean</li>
 * </ul>
 */
public interface ChatHistoryStorage {

    /**
     * 加载指定会话的历史消息（按时间升序）。
     */
    List<ChatMessage> loadHistory(String sessionId);

    /**
     * 加载最近 maxMessages 条历史（按时间升序），用于控制大模型上下文长度。
     */
    List<ChatMessage> loadHistory(String sessionId, int maxMessages);

    /**
     * 保存一条消息到指定会话。
     */
    void saveMessage(String sessionId, String role, String content);
}
