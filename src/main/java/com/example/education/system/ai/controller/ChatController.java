package com.example.education.system.ai.controller;


import com.example.education.system.ai.dto.ChatDTO;
import com.example.education.system.ai.dto.SseEventType;
import com.example.education.system.ai.service.ChatService;
import com.example.education.system.ai.support.SseEventEncoder;
import com.example.education.system.common.IgnoreResponseAdvice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final SseEventEncoder sseEventEncoder;

    @IgnoreResponseAdvice
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@Valid @RequestBody ChatDTO chatDTO) {
        return this.chatService.chat(chatDTO.getQuestion(), chatDTO.getSessionId())
                .onErrorResume(e -> {
                    log.error("ChatController.onErrorResume 捕获异常", e);
                    String msg = e.getMessage() != null ? e.getMessage() : "未知错误";
                    return Flux.just(sseEventEncoder.encode(msg, SseEventType.COMPLETE));
                });
    }

    @PostMapping("/stop")
    public void stop(@Valid @RequestBody ChatDTO chatDTO) {
        this.chatService.stop(chatDTO.getSessionId());
    }
}
