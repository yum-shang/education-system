package com.example.education.system.ai.config;

import com.example.education.system.ai.model.ChatMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 滚动摘要 Prompt 模板加载与拼装。
 */
@Component
@ConditionalOnProperty(name = AiSummaryConstants.SUMMARY_ENABLED_PROPERTY, havingValue = AiSummaryConstants.SUMMARY_ENABLED_VALUE)
public class SummaryPromptProvider {

    private static final Logger log = LoggerFactory.getLogger(SummaryPromptProvider.class);

    private static final String CLASSPATH_TEMPLATE = "classpath:prompts/summary-prompt.txt";

    private final ResourceLoader resourceLoader;
    private String template = "";

    public SummaryPromptProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            Resource resource = resourceLoader.getResource(CLASSPATH_TEMPLATE);
            template = resource.getContentAsString(StandardCharsets.UTF_8);
            log.info("已加载摘要 Prompt 模板");
        } catch (IOException e) {
            log.error("加载摘要 Prompt 失败，使用内置模板", e);
            template = """
                    请将旧摘要与新对话合并为一条中文摘要（300~500字），保留课程ID、学生方向与决策。
                    【旧摘要】
                    {oldSummary}
                    【新对话】
                    {newMessages}
                    """;
        }
    }

    /**
     * 将旧摘要与待摘要消息格式化为完整 Prompt 文本。
     */
    public String build(String oldSummary, List<ChatMessage> batch) {
        String safeOld = oldSummary == null || oldSummary.isBlank() ? "（无）" : oldSummary.trim();
        String newMessages = formatMessages(batch);
        return template
                .replace("{oldSummary}", safeOld)
                .replace("{newMessages}", newMessages);
    }

    private String formatMessages(List<ChatMessage> batch) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : batch) {
            sb.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }
        return sb.toString();
    }
}
