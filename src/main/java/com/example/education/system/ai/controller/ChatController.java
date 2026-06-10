package com.example.education.system.ai.controller;


import com.example.education.system.ai.dto.ChatDTO;
import com.example.education.system.ai.dto.SseEvent;
import com.example.education.system.ai.service.ChatService;
import com.example.education.system.common.IgnoreResponseAdvice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    private static final int EVENT_COMPLETE = 1002;

    @IgnoreResponseAdvice
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatDTO chatDTO) {
        try {
            return this.chatService.chat(chatDTO.getQuestion(), chatDTO.getSessionId())
                    .onErrorResume(e -> {
                        log.error("ChatController.onErrorResume 捕获异常", e);
                        String msg = e.getMessage() != null
                                ? e.getMessage() : "未知错误";
                        return Flux.just(sseLine(new SseEvent(msg, EVENT_COMPLETE)));
                    });
        } catch (Exception e) {
            log.error("ChatController 同步异常", e);
            String msg = e.getMessage() != null
                    ? e.getMessage() : "未知错误";
            return Flux.just(sseLine(new SseEvent(msg, EVENT_COMPLETE)));
        }
    }

    @PostMapping("/stop")
    public void stop(@RequestBody ChatDTO chatDTO) {
        this.chatService.stop(chatDTO.getSessionId());
    }


    private String sseLine(SseEvent event) {
        try {
            return "data:" + objectMapper.writeValueAsString(event) + "\n\n";
        } catch (JsonProcessingException e) {
            log.error("SseEvent 序列化失败", e);
            return "data:{\"eventData\":\"\",\"eventType\":1002}\n\n";
        }
    }
}
