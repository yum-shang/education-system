package com.example.education.system.ai.service;

/**
 * 滚动摘要编排：满 batch 条消息后异步调用大模型生成摘要并写入 MySQL。
 * 并发安全委托给 {@link SummaryConcurrencyCoordinator}。
 */
public interface RollingSummaryService {

    /**
     * 若热消息条数已达阈值，则异步触发滚动摘要。
     * 通常在 assistant 回复保存后调用。
     */
    void triggerIfNeeded(String sessionId);
}
