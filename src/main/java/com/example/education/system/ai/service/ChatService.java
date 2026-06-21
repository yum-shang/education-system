package com.example.education.system.ai.service;

import reactor.core.publisher.Flux;

public interface ChatService {

    Flux<String> chat(String question, String sessionId);

    void stop(String sessionId);


}
