package com.example.education.system.ai.config;

import com.example.education.system.ai.VO.SessionVO;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "session")
public class SessionProperties {

    private String title;

    private String describe;

    private List<SessionVO.Example> examples;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescribe() { return describe; }
    public void setDescribe(String describe) { this.describe = describe; }

    public List<SessionVO.Example> getExamples() { return examples; }
    public void setExamples(List<SessionVO.Example> examples) { this.examples = examples; }
}
