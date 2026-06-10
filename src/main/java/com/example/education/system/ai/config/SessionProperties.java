package com.example.education.system.ai.config;


import com.example.education.system.ai.VO.SessionVO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "session")
public class SessionProperties {

    private  String title;

    private String describe;

    private List<SessionVO.Example> examples;
}
