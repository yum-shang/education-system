package com.example.education.system.ai.dto;


import jakarta.validation.constraints.NotBlank;

public class ChatDTO {

    private String question;

    @NotBlank(message = "sessionId不能为空")
    private String sessionId;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
