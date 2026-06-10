package com.example.education.system.ai.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SessionVO {

    //标识AI助手会话
    private String sessionId;

    private String title;

    private  String describe;

    private List<Example> examples;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Example {
        private String title;

        private String describe;
    }

}
