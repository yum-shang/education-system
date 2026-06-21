package com.example.education.system.ai.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Legacy {@link com.example.education.system.ai.service.ChatHistoryStorage} 的条件装配。
 * <p>
 * Spring Boot 3.3 不支持在同一类上重复 {@code @ConditionalOnProperty}，
 * 也不存在 {@code @ConditionalOnProperties}，因此用自定义 {@link Condition} 组合多条件。
 */
public final class LegacyChatHistoryConditions {

    private static final String STORAGE_PROPERTY = "ai.chat-history.storage";

    private LegacyChatHistoryConditions() {
    }

    /**
     * 摘要模式未开启：{@code ai.chat.summary-enabled=false} 或未配置（默认 false）。
     */
    static boolean isSummaryDisabled(ConditionContext context) {
        Boolean enabled = context.getEnvironment().getProperty(
                AiSummaryConstants.SUMMARY_ENABLED_PROPERTY, Boolean.class, false);
        return !Boolean.TRUE.equals(enabled);
    }

    /**
     * Legacy + MySQL：{@code ai.chat-history.storage=mysql} 或未配置（默认 mysql）。
     */
    public static class Mysql implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            if (!isSummaryDisabled(context)) {
                return false;
            }
            String storage = context.getEnvironment().getProperty(STORAGE_PROPERTY, "mysql");
            return "mysql".equalsIgnoreCase(storage);
        }
    }

    /**
     * Legacy + Redis：{@code ai.chat-history.storage=redis}。
     */
    public static class Redis implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            if (!isSummaryDisabled(context)) {
                return false;
            }
            String storage = context.getEnvironment().getProperty(STORAGE_PROPERTY);
            return "redis".equalsIgnoreCase(storage);
        }
    }
}
