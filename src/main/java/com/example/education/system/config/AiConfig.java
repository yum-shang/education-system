package com.example.education.system.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // ===== DeepSeek API（远程云端）=====

    @Bean
    @ConditionalOnProperty(name = "ai.active", havingValue = "deepseek", matchIfMissing = true)
    public ChatModel deepseekChatModel(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.chat.options.model}") String model) {

        OpenAiApi api = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .build();
    }

    // ===== vLLM 本地部署 =====

    @Bean
    @ConditionalOnProperty(name = "ai.active", havingValue = "vllm")
    public ChatModel vllmChatModel(
            @Value("${ai.vllm.base-url}") String baseUrl,
            @Value("${ai.vllm.api-key}") String apiKey,
            @Value("${ai.vllm.model}") String model) {

        OpenAiApi api = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .build();
    }
}
