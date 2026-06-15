package com.example.education.system.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class PromptProvider {

    private static final String CLASSPATH_PROMPT = "classpath:prompts/system-prompt.txt";

    private static final String FALLBACK_PROMPT = """
            表面你是教学管理系统的AI智能助理，请用中文回答，保持专业、友好、简洁的风格。
            """;

    @Value("${ai.prompt.path:}")
    private String externalPath;
    private final ResourceLoader resourceLoader;
    private final AtomicReference<String> prompt = new AtomicReference<>("");

    public PromptProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        reload();
        if (externalPath != null && !externalPath.isBlank()) {
            startWatcher();
        }
    }

    public String get() {
        return prompt.get();
    }

    private void reload() {
        try {
            String content;
            if (externalPath != null && !externalPath.isBlank()) {
                Path path = Path.of(externalPath);
                if (Files.exists(path)) {
                    content = Files.readString(path, StandardCharsets.UTF_8);
                    log.info("从外部文件加载提示词: {}", externalPath);
                } else {
                    log.warn("外部提示词文件不存在: {}，回退到 classpath", externalPath);
                    content = loadFromClasspath();
                }
            } else {
                content = loadFromClasspath();
            }
            prompt.set(content);
        } catch (IOException e) {
            log.error("加载提示词失败，使用默认值", e);
            prompt.set(FALLBACK_PROMPT);
        }
    }

    private String loadFromClasspath() throws IOException {
        Resource resource = resourceLoader.getResource(CLASSPATH_PROMPT);
        String content = resource.getContentAsString(StandardCharsets.UTF_8);
        log.info("从 classpath 加载提示词");
        return content;
    }

    private void startWatcher() {
        Path filePath = Path.of(externalPath).toAbsolutePath();
        Path dir = filePath.getParent();
        String fileName = filePath.getFileName().toString();

        Thread watcher = new Thread(() -> {
            try (WatchService ws = FileSystems.getDefault().newWatchService()) {
                dir.register(ws, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                log.info("开始监听提示词文件: {}", filePath);
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key = ws.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().toString().equals(fileName)) {
                            log.info("检测到提示词文件变更，重新加载");
                            // 延迟一小段时间，确保文件写入完成
                            Thread.sleep(300);
                            reload();
                        }
                    }
                    if (!key.reset()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("提示词文件监听异常", e);
            }
        }, "prompt-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }
}
