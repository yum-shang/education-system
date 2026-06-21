package com.example.education.system.ai.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.sql.Timestamp;

/**
 * AI 聊天消息实体，对应 {@code ai_messages} 表。
 * <p>
 * 手写 getter/setter，避免 Lombok 注解处理未启用时该类无法编译，
 * 进而导致引用方报「找不到符号 ChatMessage」。
 */
@TableName("ai_messages")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    /** 角色：user / assistant */
    private String role;

    private String content;

    private Timestamp createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
