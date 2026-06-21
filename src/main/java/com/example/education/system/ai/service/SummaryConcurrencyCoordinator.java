package com.example.education.system.ai.service;

import com.example.education.system.ai.model.ChatMessage;

import java.util.List;

/**
 * 滚动摘要的并发协调器：分布式锁、Redis RENAME 快照、成功/失败状态迁移。
 * <p>
 * 实现类：{@link com.example.education.system.ai.service.impl.RedisSummaryConcurrencyCoordinator}
 */
public interface SummaryConcurrencyCoordinator {

    boolean tryLock(String sessionId);

    void releaseLock(String sessionId);

    List<ChatMessage> captureBatchForSummary(String sessionId);

    void onSummarySuccess(String sessionId, List<ChatMessage> summarizedBatch);

    void onSummaryFailure(String sessionId, List<ChatMessage> batch);

    default boolean isSummarizing(String sessionId) {
        return false;
    }
}
