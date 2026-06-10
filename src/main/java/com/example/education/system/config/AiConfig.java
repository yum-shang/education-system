package com.example.education.system.config;

import com.example.education.system.courses.tool.CourseQueryTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /**
     * 创建 ChatClient，并将所有 @Tool 工具 Bean 注册进去。
     * defaultTools() 会扫描传入 Bean 中的 @Tool 方法，生成工具定义随每次请求发给 AI。
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel, CourseQueryTools courseQueryTools) {
        return ChatClient.builder(chatModel)
                .defaultTools(courseQueryTools)
                .build();
    }

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
