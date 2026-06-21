package com.example.education.system;

import com.example.education.system.ai.config.AiChatProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = OpenAiAutoConfiguration.class)
@EnableAsync
@EnableConfigurationProperties(AiChatProperties.class)
public class EducationSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationSystemApplication.class, args);
    }
}