package com.example.education.system.ai.service;

import reactor.core.publisher.Flux;

public interface ChatService {

    Flux<String> chat(String question,String sessionID);

    void stop(String sessionId);
}
