package com.example.education.system.ai.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("ai_sessions")
public class ChatSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    private Integer userId;

    private String title;

    private Timestamp createTime;

    private Timestamp updateTime;

    private Long creator;

    private Long updater;
}
