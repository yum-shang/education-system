package com.example.education.system.ai.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.sql.Timestamp;

/** AI 会话实体，对应 {@code ai_sessions} 表 */
@TableName("ai_sessions")
public class ChatSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    private Integer userId;

    private String title;

    private Timestamp createTime;

    private Timestamp updateTime;

    /** 会话过期时间，驱动 Redis TTL（滑动续期） */
    private Timestamp expireAt;

    /** active / closed / expired */
    private String status;

    private Long creator;

    private Long updater;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }

    public Timestamp getUpdateTime() { return updateTime; }
    public void setUpdateTime(Timestamp updateTime) { this.updateTime = updateTime; }

    public Timestamp getExpireAt() { return expireAt; }
    public void setExpireAt(Timestamp expireAt) { this.expireAt = expireAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCreator() { return creator; }
    public void setCreator(Long creator) { this.creator = creator; }

    public Long getUpdater() { return updater; }
    public void setUpdater(Long updater) { this.updater = updater; }
}
