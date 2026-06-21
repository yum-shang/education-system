package com.example.education.system.ai.service;

import com.example.education.system.ai.model.ChatSessionSummary;

import java.util.Optional;

/**
 * MySQL 滚动摘要读写。
 */
public interface SessionSummaryService {

    Optional<ChatSessionSummary> findBySessionId(String sessionId);

    /**
     * 滚动更新摘要：替换 summary_content，covered_count 累加，version +1。
     */
    void upsertRolling(String sessionId, String summaryContent, int batchMessageCount);
}
