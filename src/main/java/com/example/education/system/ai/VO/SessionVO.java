package com.example.education.system.ai.VO;

import java.util.List;


//会话类，调用工具
public class SessionVO {

    private String sessionId;

    private String title;

    private String describe;

    private List<Example> examples;

    public SessionVO() {
    }

    public SessionVO(String sessionId, String title, String describe, List<Example> examples) {
        this.sessionId = sessionId;
        this.title = title;
        this.describe = describe;
        this.examples = examples;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescribe() { return describe; }
    public void setDescribe(String describe) { this.describe = describe; }

    public List<Example> getExamples() { return examples; }
    public void setExamples(List<Example> examples) { this.examples = examples; }

    //调用工具？，内部静态类
    public static class Example {
        private String title;
        private String describe;

        public Example() {
        }

        public Example(String title, String describe) {
            this.title = title;
            this.describe = describe;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescribe() { return describe; }
        public void setDescribe(String describe) { this.describe = describe; }
    }
}
