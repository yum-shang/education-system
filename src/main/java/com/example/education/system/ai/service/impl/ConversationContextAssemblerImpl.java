package com.example.education.system.ai.service.impl;

import com.example.education.system.ai.config.AiSummaryConstants;
import com.example.education.system.ai.config.PromptProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.example.education.system.ai.model.ChatMessage;
import com.example.education.system.ai.service.ConversationContextAssembler;
import com.example.education.system.ai.service.RedisHotMessageStore;
import com.example.education.system.ai.service.SessionSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 上下文组装：System + MySQL 滚动摘要 + Redis（快照 + 热消息）+ 当前问题。
 * <p>
 * 摘要进行中时，待摘要 batch 仍在 snapshot 里，需与 hot 一并拼入，避免短暂「失忆」。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = AiSummaryConstants.SUMMARY_ENABLED_PROPERTY, havingValue = AiSummaryConstants.SUMMARY_ENABLED_VALUE)
public class ConversationContextAssemblerImpl implements ConversationContextAssembler {

    private static final String SUMMARY_PREFIX = "【历史摘要】\n";

    private final PromptProvider promptProvider;
    private final SessionSummaryService sessionSummaryService;
    private final RedisHotMessageStore redisHotMessageStore;

    @Override
    public List<Message> assemble(String sessionId, String question) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(promptProvider.get()));

        sessionSummaryService.findBySessionId(sessionId).ifPresent(summary -> {
            String content = summary.getSummaryContent();
            if (content != null && !content.isBlank()) {
                messages.add(new UserMessage(SUMMARY_PREFIX + content.trim()));
            }
        });

        // snapshot（待摘要）在前，hot（摘要期间新消息）在后，保持时间顺序
        List<ChatMessage> pending = new ArrayList<>();
        pending.addAll(redisHotMessageStore.loadSnapshot(sessionId));
        pending.addAll(redisHotMessageStore.loadAll(sessionId));
        appendChatMessages(messages, pending);

        messages.add(new UserMessage(question));
        return messages;
    }

    private void appendChatMessages(List<Message> messages, List<ChatMessage> chatMessages) {
        for (ChatMessage msg : chatMessages) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
    }
}
