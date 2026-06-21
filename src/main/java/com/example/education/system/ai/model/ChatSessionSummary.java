package com.example.education.system.ai.model;



import com.baomidou.mybatisplus.annotation.IdType;

import com.baomidou.mybatisplus.annotation.TableId;

import com.baomidou.mybatisplus.annotation.TableName;



import java.sql.Timestamp;



/**

 * AI 会话滚动摘要实体，对应 {@code ai_session_summaries} 表。

 * 每个 session 仅保留最新一条摘要（滚动更新）。

 */

@TableName("ai_session_summaries")

public class ChatSessionSummary {



    @TableId(type = IdType.AUTO)

    private Long id;



    private String sessionId;



    private String summaryContent;



    /** 已纳入摘要的消息条数（user + assistant 合计） */

    private Integer coveredCount;



    /** 每次滚动摘要成功后 +1 */

    private Integer version;



    private Timestamp createTime;



    private Timestamp updateTime;



    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }



    public String getSessionId() { return sessionId; }

    public void setSessionId(String sessionId) { this.sessionId = sessionId; }



    public String getSummaryContent() { return summaryContent; }

    public void setSummaryContent(String summaryContent) { this.summaryContent = summaryContent; }



    public Integer getCoveredCount() { return coveredCount; }

    public void setCoveredCount(Integer coveredCount) { this.coveredCount = coveredCount; }



    public Integer getVersion() { return version; }

    public void setVersion(Integer version) { this.version = version; }



    public Timestamp getCreateTime() { return createTime; }

    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }



    public Timestamp getUpdateTime() { return updateTime; }

    public void setUpdateTime(Timestamp updateTime) { this.updateTime = updateTime; }

}

