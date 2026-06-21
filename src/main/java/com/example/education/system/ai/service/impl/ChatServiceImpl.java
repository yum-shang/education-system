package com.example.education.system.ai.service.impl;

import com.example.education.system.ai.config.AiChatProperties;
import com.example.education.system.ai.config.PromptProvider;
import com.example.education.system.ai.dto.SseEventType;
import com.example.education.system.ai.model.ChatMessage;
import com.example.education.system.ai.service.ChatHistoryStorage;
import com.example.education.system.ai.service.ChatService;
import com.example.education.system.ai.service.ChatSessionService;
import com.example.education.system.ai.service.ConversationContextAssembler;
import com.example.education.system.ai.service.RedisHotMessageStore;
import com.example.education.system.ai.service.RollingSummaryService;
import com.example.education.system.ai.support.SseEventEncoder;
import com.example.education.system.courses.tool.CourseQueryTools;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatClient chatClient;
    private final ObjectProvider<ChatHistoryStorage> chatHistoryStorageProvider;
    private final ChatSessionService chatSessionService;
    private final SseEventEncoder sseEventEncoder;
    private final PromptProvider promptProvider;
    private final CourseQueryTools courseQueryTools;
    private final AiChatProperties aiChatProperties;

    /** 摘要模式 Bean，仅在 ai.chat.summary-enabled=true 时装配 */
    private final ObjectProvider<ConversationContextAssembler> contextAssemblerProvider;
    private final ObjectProvider<RedisHotMessageStore> hotMessageStoreProvider;
    private final ObjectProvider<RollingSummaryService> rollingSummaryServiceProvider;

    private final StringRedisTemplate stringRedisTemplate;

    private static final String CANCEL_KEY_PREFIX = "ai:cancel:";
    private static final Duration CANCEL_TTL = Duration.ofMinutes(30);

    @Override
    public Flux<String> chat(String question, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Flux.just(sseEventEncoder.encode("sessionId 不能为空", SseEventType.COMPLETE));
        }
        if (question == null || question.isBlank()) {
            return Flux.just(sseEventEncoder.encode("question 不能为空", SseEventType.COMPLETE));
        }
        if (!chatSessionService.isOwnedByCurrentUser(sessionId)) {
            return Flux.just(sseEventEncoder.encode("无权访问该会话", SseEventType.COMPLETE));
        }

        if (aiChatProperties.isSummaryEnabled()) {
            return chatWithRollingSummary(question, sessionId);
        }
        return chatLegacy(question, sessionId);
    }

    private Flux<String> chatWithRollingSummary(String question, String sessionId) {
        ConversationContextAssembler assembler = contextAssemblerProvider.getIfAvailable();
        RedisHotMessageStore hotStore = hotMessageStoreProvider.getIfAvailable();
        RollingSummaryService rollingSummary = rollingSummaryServiceProvider.getIfAvailable();
        if (assembler == null || hotStore == null || rollingSummary == null) {
            log.error("摘要模式已开启但摘要组件未装配, sessionId={}", sessionId);
            return Flux.just(sseEventEncoder.encode("摘要服务未就绪", SseEventType.COMPLETE));
        }

        try {
            chatSessionService.ensureSessionActive(sessionId);
            chatSessionService.touchSession(sessionId);
            hotStore.refreshTtl(sessionId);
        } catch (ResponseStatusException e) {
            return Flux.just(sseEventEncoder.encode(e.getReason(), SseEventType.COMPLETE));
        }

        List<Message> messages = assembler.assemble(sessionId, question);

        try {
            hotStore.saveMessage(sessionId, "user", question);
        } catch (Exception e) {
            log.error("保存热消息(user)失败, sessionId={}", sessionId, e);
        }

        return streamChat(sessionId, messages, (answer, sid) -> {
            try {
                if (!answer.isEmpty()) {
                    hotStore.saveMessage(sid, "assistant", answer);
                }
                rollingSummary.triggerIfNeeded(sid);
            } catch (Exception e) {
                log.error("保存热消息(assistant)或触发摘要失败, sessionId={}", sid, e);
            }
        });
    }

    private Flux<String> chatLegacy(String question, String sessionId) {
        ChatHistoryStorage chatHistoryStorage = chatHistoryStorageProvider.getIfAvailable();
        if (chatHistoryStorage == null) {
            log.error("Legacy 模式缺少 ChatHistoryStorage Bean, sessionId={}", sessionId);
            return Flux.just(sseEventEncoder.encode("聊天存储未配置", SseEventType.COMPLETE));
        }

        int maxHistory = aiChatProperties.getMaxHistoryMessages();
        List<ChatMessage> history = chatHistoryStorage.loadHistory(sessionId, maxHistory);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(promptProvider.get()));
        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        messages.add(new UserMessage(question));

        try {
            chatHistoryStorage.saveMessage(sessionId, "user", question);
        } catch (Exception e) {
            log.error("保存用户消息失败, sessionId={}", sessionId, e);
        }

        return streamChat(sessionId, messages, (answer, sid) -> {
            try {
                if (!answer.isEmpty()) {
                    chatHistoryStorage.saveMessage(sid, "assistant", answer);
                }
            } catch (Exception e) {
                log.error("保存AI回复失败, sessionId={}", sid, e);
            }
        });
    }

    private Flux<String> streamChat(String sessionId, List<Message> messages,
                                    AssistantPersistCallback onComplete) {
        String requestId = UUID.randomUUID().toString();
        StringBuilder fullAnswer = new StringBuilder();
        String cancelKey = CANCEL_KEY_PREFIX + sessionId;

        return chatClient.prompt()
                .messages(messages.toArray(new Message[0]))
                .toolContext(Map.of(CourseQueryTools.CONTEXT_REQUEST_ID, requestId))
                .stream()
                .content()
                .publishOn(Schedulers.boundedElastic())
                .takeWhile(chunk -> !Boolean.TRUE.equals(stringRedisTemplate.hasKey(cancelKey)))
                .doOnNext(fullAnswer::append)
                .map(chunk -> sseEventEncoder.encode(chunk, SseEventType.TEXT))
                .concatWith(Flux.defer(() -> {
                    boolean wasCancelled = Boolean.TRUE.equals(stringRedisTemplate.hasKey(cancelKey));
                    stringRedisTemplate.delete(cancelKey);
                    String answer = fullAnswer.toString();
                    if (!answer.isEmpty()) {
                        onComplete.persist(answer, sessionId);
                    }
                    if (wasCancelled) {
                        return Flux.empty();
                    }
                    List<Map<String, Object>> courseEvents = courseQueryTools.takeCourseEvents(requestId);
                    Flux<String> sseCourse = Flux.empty();
                    if (courseEvents != null && !courseEvents.isEmpty()) {
                        sseCourse = Flux.fromIterable(courseEvents)
                                .map(data -> sseEventEncoder.encode(data, SseEventType.COURSE));
                    }
                    return Flux.concat(sseCourse, Flux.just(sseEventEncoder.encode("", SseEventType.COMPLETE)));
                }))
                .doFinally(signalType -> {
                    stringRedisTemplate.delete(cancelKey);
                    courseQueryTools.takeCourseEvents(requestId);
                })
                .onErrorComplete(e -> true)
                .onErrorResume(e -> {
                    stringRedisTemplate.delete(cancelKey);
                    String msg = e.getMessage() != null ? e.getMessage() : "未知错误";
                    return Flux.just(sseEventEncoder.encode(msg, SseEventType.COMPLETE));
                });
    }

    @Override
    public void stop(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId 不能为空");
        }
        if (!chatSessionService.isOwnedByCurrentUser(sessionId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该会话");
        }
        stringRedisTemplate.opsForValue().set(CANCEL_KEY_PREFIX + sessionId, "1", CANCEL_TTL);
    }

    @FunctionalInterface
    private interface AssistantPersistCallback {
        void persist(String answer, String sessionId);
    }
}
