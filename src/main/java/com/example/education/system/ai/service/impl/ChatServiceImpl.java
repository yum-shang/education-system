package com.example.education.system.ai.service.impl;

import com.example.education.system.ai.config.PromptProvider;
import com.example.education.system.ai.dto.SseEvent;
import com.example.education.system.ai.model.ChatMessage;
import com.example.education.system.ai.service.ChatHistoryStorage;
import com.example.education.system.ai.service.ChatService;
import com.example.education.system.courses.tool.CourseQueryTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI 对话服务实现。
 *
 * <h3>取消机制原理（Redis 实现）</h3>
 * <ol>
 *   <li>调用 {@link #stop(String)} 时，向 Redis 写入一个 Key（{@code ai:cancel:{sessionId}}），
 *       TTL 30 分钟自动过期。</li>
 *   <li>{@link #chat(String, String)} 的流式管道中，{@code takeWhile} 每收到一个 chunk
 *       就查一次 Redis 该 Key 是否存在。</li>
 *   <li>Key 存在 → {@code takeWhile} 返回 false → Flux 正常完成 → 触发 {@code concatWith}
 *       保存已收到的部分回复。</li>
 *   <li>无论正常完成、被取消还是异常，都在退出路径中清理 Redis Key。</li>
 * </ol>
 *
 * <h4>为什么用 Redis 而不是 ConcurrentHashMap？</h4>
 * <ul>
 *   <li><b>分布式</b>：多实例部署时，A 实例发起的流，B 实例也能取消。</li>
 *   <li><b>持久化</b>：服务重启后取消标记不丢失。</li>
 *   <li><b>自动过期</b>：TTL 兜底清理，不会因逻辑遗漏导致内存泄漏。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ChatHistoryStorage chatHistoryStorage;
    private final ObjectMapper objectMapper;
    private final PromptProvider promptProvider;
    private final CourseQueryTools courseQueryTools;

    /**
     * Spring Boot 自动配置的同步 Redis 模板。
     * 选择 StringRedisTemplate（非 reactive）是因为 takeWhile 的断⾔是同步的，
     * 用同步 API 可以直接在谓词中快速判断，无需重构整个管道。
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis 取消标记的 Key 前缀。
     * 完整格式：{@code ai:cancel:} + sessionId，如 {@code ai:cancel:abc123}
     */
    private static final String CANCEL_KEY_PREFIX = "ai:cancel:";

    /**
     * 取消标记的存活时间（TTL）。
     * 30 分钟后 Redis 自动删除 Key，作为兜底防止内存泄漏。
     * 正常流程中流结束时会主动删除，不需要活这么久。
     */
    private static final Duration CANCEL_TTL = Duration.ofMinutes(30);

    private static final int EVENT_TEXT = 1001;
    private static final int EVENT_COMPLETE = 1002;
    private static final int EVENT_COURSE = 1003;

    @Override
    @PreAuthorize("isAuthenticated()")
    public Flux<String> chat(String question, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Flux.just(sseLine(new SseEvent("sessionId 不能为空", EVENT_COMPLETE)));
        }
        if (question == null || question.isBlank()) {
            return Flux.just(sseLine(new SseEvent("question 不能为空", EVENT_COMPLETE)));
        }

        // 1. 加载历史消息
        List<ChatMessage> history = chatHistoryStorage.loadHistory(sessionId);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(promptProvider.get()));
        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        messages.add(new UserMessage(question));

        // 2. 保存用户消息
        try {
            chatHistoryStorage.saveMessage(sessionId, "user", question);
        } catch (Exception e) {
            log.error("保存用户消息失败, sessionId={}", sessionId, e);
        }

        // 3. 生成本请求唯一 ID，注册为活跃请求（工具方法通过此表写入课程数据）
        String requestId = UUID.randomUUID().toString();
        courseQueryTools.registerActiveRequest(requestId);

        // 4. 调用 AI 流式返回，包装为 SSE data: 格式
        StringBuilder fullAnswer = new StringBuilder();
        String cancelKey = CANCEL_KEY_PREFIX + sessionId;

        return chatClient.prompt()
                .messages(messages.toArray(new Message[0]))
                .stream()
                .content()
                        /*
                         * ============================================================
                         * 取消检查（Redis 实现）
                         *
                         * 工作原理：
                         *   stop(sessionId) 调用后，Redis 中会存在 "ai:cancel:{sessionId}" 这个 Key。
                         *   takeWhile 每收到一个 AI 返回的文本 chunk，就检查一次该 Key：
                         *     - Key 不存在 → takeWhile 返回 true  → chunk 继续流向下游（前端展示）
                         *     - Key 存在   → takeWhile 返回 false → Flux 正常结束
                         *       → 触发 concatWith 把已收到的部分回复存库
                         *       → 清理 Redis Key
                         *
                         * 性能备注：
                         *   hasKey(key) 是 Redis 的 EXISTS 命令，O(1) 操作，本地延迟 < 1ms。
                         *   AI 流式 chunk 间隔通常在几百毫秒，CHECK 开销完全可以忽略。
                         * ============================================================
                         */
                        .takeWhile(chunk -> {
                            Boolean cancelled = stringRedisTemplate.hasKey(cancelKey);
                            if (Boolean.TRUE.equals(cancelled)) {
                                log.info("检测到 Redis 取消标记，中止AI流, sessionId={}", sessionId);
                            }
                            return !Boolean.TRUE.equals(cancelled);
                        })
                        .doOnNext(fullAnswer::append)
                        .map(chunk -> sseLine(new SseEvent(chunk, EVENT_TEXT)))
                        /*
                         * concatWith + Flux.defer：
                         * 上游 Flux 完成（正常结束 / takeWhile 取消 / 异常）后，
                         * 追加一个 SSE 完成事件。这里同时负责：
                         *   1. 删除 Redis 取消 Key（无论哪种结束方式）
                         *   2. 将 AI 已回复的部分内容写入数据库
                         *   3. 发送 EVENT_COMPLETE 让前端关闭连接
                         */
                        /*
                         * concatWith + Flux.defer：
                         * 上游 Flux 完成（正常结束 / takeWhile 取消）后执行。
                         *
                         * 关键判断：如果 Redis 取消 Key 此时还存在，说明流是被 stop() 取消的
                         * ——此时客户端已经主动关闭了 SSE 连接，不应再尝试写入 EVENT_COMPLETE，
                         * 否则 Spring 对已断开连接写入会触发 AsyncContext 异常。
                         * 返回 Flux.empty() 让流静默结束即可。
                         *
                         * 如果 Key 不存在，说明是 AI 自然结束，正常发送 EVENT_COMPLETE。
                         */
                        .concatWith(Flux.defer(() -> {
                            boolean wasCancelled = Boolean.TRUE.equals(stringRedisTemplate.hasKey(cancelKey));
                            stringRedisTemplate.delete(cancelKey);
                            try {
                                String answer = fullAnswer.toString();
                                if (!answer.isEmpty()) {
                                    chatHistoryStorage.saveMessage(sessionId, "assistant", answer);
                                }
                            } catch (Exception e) {
                                log.error("保存AI回复失败, sessionId={}", sessionId, e);
                            }
                            if (wasCancelled) {
                                log.info("流已被取消，跳过发送完成事件, sessionId={}", sessionId);
                                return Flux.empty();
                            }
                            // 取出本次请求中工具查到的课程数据，输出 1003 事件
                            List<Map<String, Object>> courseEvents = courseQueryTools.takeCourseEvents(requestId);
                            Flux<String> sseCourse = Flux.empty();
                            if (courseEvents != null && !courseEvents.isEmpty()) {
                                sseCourse = Flux.fromIterable(courseEvents)
                                        .map(data -> sseLine(new SseEvent(data, EVENT_COURSE)));
                            }
                            // 顺序：所有 1003（课程卡片）→ 1002（完成）
                            return Flux.concat(sseCourse, Flux.just(sseLine(new SseEvent("", EVENT_COMPLETE))));
                        }))
                        /*
                         * doFinally：无论流以何种方式结束（正常/取消/异常），都执行一次兜底清理。
                         * 这是最后一道防线，确保 Redis Key 不会残留。
                         */
                        .doFinally(signalType -> {
                            stringRedisTemplate.delete(cancelKey);
                            courseQueryTools.takeCourseEvents(requestId);
                            log.debug("流结束, signalType={}, sessionId={}", signalType, sessionId);
                        })
                        .doOnError(e -> {
                            stringRedisTemplate.delete(cancelKey);
                            log.error("AI对话异常, sessionId={}", sessionId, e);
                        })
                        /*
                         * onErrorComplete：当 concatWith 尝试写入 EVENT_COMPLETE 到已断开连接
                         * 抛出 IOException/IllegalStateException 时，将其视为正常完成而非异常。
                         * 避免 Spring ReactiveTypeHandler 因 AsyncContext 状态异常而打印堆栈。
                         */
                        .onErrorComplete(e -> {
                            log.debug("写入完成事件失败（客户端可能已断开）, sessionId={}, error={}",
                                    sessionId, e.getMessage());
                            return true;
                        })
                        .onErrorResume(e -> {
                            stringRedisTemplate.delete(cancelKey);
                            log.error("AI流已被onErrorResume捕获, sessionId={}", sessionId, e);
                            String msg = e.getMessage() != null ? e.getMessage() : "未知错误";
                            return Flux.just(sseLine(new SseEvent(msg, EVENT_COMPLETE)));
                        });
    }

    /**
     * 取消指定会话正在进行的 AI 流。
     *
     * <h4>工作流程</h4>
     * <ol>
     *   <li>向 Redis 写入 {@code ai:cancel:{sessionId}=1}，TTL 30 分钟</li>
     *   <li>{@link #chat} 中的 {@code takeWhile} 在下一个 chunk 到来时
     *       通过 {@code hasKey()} 检测到该 Key，返回 false，流安全结束</li>
     *   <li>流结束后 {@code concatWith} 清理 Key 并保存部分回复</li>
     *   <li>如果 30 分钟内没有任何 chunk（极端情况），Key 自动过期，不残留</li>
     * </ol>
     *
     * @param sessionId 要取消的会话 ID
     */
    @Override
    public void stop(String sessionId) {
        String cancelKey = CANCEL_KEY_PREFIX + sessionId;
        /*
         * opsForValue().set(key, value, ttl)：
         *   - key: ai:cancel:{sessionId}
         *   - value: "1"（占位值，只要 Key 存在就行）
         *   - ttl: 30 分钟，过期自动删除
         *
         * 不需要判断返回值，因为这个 Key 只要被写入就算成功。
         */
        stringRedisTemplate.opsForValue().set(cancelKey, "1", CANCEL_TTL);
        log.info("已设置 Redis 取消标记, key={}, ttl={}", cancelKey, CANCEL_TTL);
    }

    /**
     * 将 SseEvent 序列化为 SSE 协议的 {@code data:} 行。
     * 格式：{@code data:{"eventData":"...","eventType":1001}\n\n}
     */
    private String sseLine(SseEvent event) {
        try {
            return "data:" + objectMapper.writeValueAsString(event) + "\n\n";
        } catch (JsonProcessingException e) {
            log.error("SseEvent 序列化失败", e);
            return "data:{\"eventData\":\"\",\"eventType\":1002}\n\n";
        }
    }


}
