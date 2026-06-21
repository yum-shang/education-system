package com.example.education.system.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * AI 摘要异步任务线程池，避免 @Async 使用默认无界线程策略。
 */
@Configuration
public class AiAsyncConfig {

    public static final String SUMMARY_EXECUTOR = "summaryTaskExecutor";

    @Bean(name = SUMMARY_EXECUTOR)
    public Executor summaryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-summary-");
        executor.initialize();
        return executor;
    }
}
