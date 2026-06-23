package com.example.education.system.ai.service.impl;

import com.example.education.system.ai.config.AiAsyncConfig;
import com.example.education.system.ai.config.AiChatProperties;
import com.example.education.system.ai.config.AiSummaryConstants;
import com.example.education.system.ai.config.SummaryPromptProvider;
import com.example.education.system.ai.model.ChatMessage;
import com.example.education.system.ai.model.ChatSessionSummary;
import com.example.education.system.ai.service.RedisHotMessageStore;
import com.example.education.system.ai.service.RollingSummaryService;
import com.example.education.system.ai.service.SessionSummaryService;
import com.example.education.system.ai.service.SummaryConcurrencyCoordinator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 滚动摘要编排：达到 batch 阈值后异步调用大模型，并发细节委托 {@link SummaryConcurrencyCoordinator}。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = AiSummaryConstants.SUMMARY_ENABLED_PROPERTY, havingValue = AiSummaryConstants.SUMMARY_ENABLED_VALUE)
public class RollingSummaryServiceImpl implements RollingSummaryService {

    private static final Logger log = LoggerFactory.getLogger(RollingSummaryServiceImpl.class);

    private final AiChatProperties aiChatProperties;
    private final RedisHotMessageStore redisHotMessageStore;
    private final SessionSummaryService sessionSummaryService;
    private final SummaryPromptProvider summaryPromptProvider;//对提示词进行拼接
    private final SummaryConcurrencyCoordinator concurrencyCoordinator;
    private final ChatClient chatClient;

    @Override
    @Async(AiAsyncConfig.SUMMARY_EXECUTOR)
    public void triggerIfNeeded(String sessionId) {

        //只有配置了总结，会话数达到了上限，的才能获得本次的锁
        if (!aiChatProperties.isSummaryEnabled()) {
            return;
        }
        int batchSize = aiChatProperties.getSummaryBatchSize();
        if (redisHotMessageStore.getCount(sessionId) < batchSize) {
            return;
        }
        if (!concurrencyCoordinator.tryLock(sessionId)) {
            log.debug("未获取摘要锁，跳过本次触发, sessionId={}", sessionId);
            return;
        }

        //拿到锁
        List<ChatMessage> batch = null;
        try {
            batch = concurrencyCoordinator.captureBatchForSummary(sessionId);
            if (batch == null || batch.isEmpty()) {
                return;
            }

            //拼接上下文
            String oldSummary = sessionSummaryService.findBySessionId(sessionId)
                    .map(ChatSessionSummary::getSummaryContent)
                    .orElse("");

            String prompt = summaryPromptProvider.build(oldSummary, batch);
            String newSummary = chatClient.prompt()
                    .user(prompt)
                    .call()//掉大模型
                    .content();//获得回答


            sessionSummaryService.upsertRolling(sessionId, newSummary, batch.size());
            concurrencyCoordinator.onSummarySuccess(sessionId, batch);
            log.info("滚动摘要完成, sessionId={}, batchSize={}", sessionId, batch.size());
        } catch (Exception e) {
            log.error("滚动摘要失败, sessionId={}", sessionId, e);
            if (batch != null) {
                concurrencyCoordinator.onSummaryFailure(sessionId, batch);
            } else if (redisHotMessageStore.hasSnapshot(sessionId)) {
                // capture 后、LLM 前抛错时 batch 可能为 null，仍需回滚 snapshot
                concurrencyCoordinator.onSummaryFailure(sessionId, List.of());
            }
        } finally {
            concurrencyCoordinator.releaseLock(sessionId);//释放锁
        }
    }
}
