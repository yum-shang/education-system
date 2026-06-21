package com.example.education.system.ai.support;

import com.example.education.system.ai.dto.SseEvent;
import com.example.education.system.ai.dto.SseEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 将 {@link SseEvent} 序列化为 SSE 协议行：{@code data:{...}\n\n}。
 * <p>
 * Controller 与 Service 共用，避免 sseLine 重复实现。
 */
@Component
@RequiredArgsConstructor
public class SseEventEncoder {

    private static final Logger log = LoggerFactory.getLogger(SseEventEncoder.class);

    private final ObjectMapper objectMapper;

    public String encode(SseEvent event) {
        try {
            return "data:" + objectMapper.writeValueAsString(event) + "\n\n";
        } catch (JsonProcessingException e) {
            log.error("SseEvent 序列化失败", e);
            return "data:{\"eventData\":\"\",\"eventType\":" + SseEventType.COMPLETE.getCode() + "}\n\n";
        }
    }

    public String encode(Object eventData, SseEventType type) {
        return encode(new SseEvent(eventData, type.getCode()));
    }
}
