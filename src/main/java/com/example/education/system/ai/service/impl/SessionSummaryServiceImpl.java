package com.example.education.system.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.education.system.ai.config.AiSummaryConstants;
import com.example.education.system.ai.model.ChatSessionSummary;
import com.example.education.system.ai.repository.ChatSessionSummaryMapper;
import com.example.education.system.ai.service.SessionSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = AiSummaryConstants.SUMMARY_ENABLED_PROPERTY, havingValue = AiSummaryConstants.SUMMARY_ENABLED_VALUE)
public class SessionSummaryServiceImpl implements SessionSummaryService {

    private final ChatSessionSummaryMapper chatSessionSummaryMapper;

    @Override
    public Optional<ChatSessionSummary> findBySessionId(String sessionId) {
        ChatSessionSummary summary = chatSessionSummaryMapper.selectOne(
                new LambdaQueryWrapper<ChatSessionSummary>()
                        .eq(ChatSessionSummary::getSessionId, sessionId)
        );
        return Optional.ofNullable(summary);
    }

    @Override
    public void upsertRolling(String sessionId, String summaryContent, int batchMessageCount) {
        Optional<ChatSessionSummary> existing = findBySessionId(sessionId);
        Timestamp now = new Timestamp(System.currentTimeMillis());

        if (existing.isPresent()) {
            ChatSessionSummary summary = existing.get();
            summary.setSummaryContent(summaryContent);
            summary.setCoveredCount(summary.getCoveredCount() + batchMessageCount);
            summary.setVersion(summary.getVersion() + 1);
            summary.setUpdateTime(now);
            chatSessionSummaryMapper.updateById(summary);
        } else {
            ChatSessionSummary summary = new ChatSessionSummary();
            summary.setSessionId(sessionId);
            summary.setSummaryContent(summaryContent);
            summary.setCoveredCount(batchMessageCount);
            summary.setVersion(1);
            summary.setCreateTime(now);
            summary.setUpdateTime(now);
            chatSessionSummaryMapper.insert(summary);
        }
    }
}
