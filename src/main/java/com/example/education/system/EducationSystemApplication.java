package com.example.education.system;

import org.springframework.boot.SpringApplication;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = OpenAiAutoConfiguration.class)
public class EducationSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationSystemApplication.class, args);
    }
}