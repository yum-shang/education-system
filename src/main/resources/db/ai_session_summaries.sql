-- AI 滚动摘要表 + 会话生命周期字段（在已有库上执行）

USE education_system;

-- 会话表扩展（若列已存在请跳过对应语句）
ALTER TABLE ai_sessions
    ADD COLUMN expire_at DATETIME NULL COMMENT '会话过期时间（滑动续期）' AFTER update_time;
ALTER TABLE ai_sessions
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT 'active/closed/expired' AFTER expire_at;

-- 每个 session 仅保留最新一条滚动摘要
CREATE TABLE IF NOT EXISTS ai_session_summaries (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    session_id      VARCHAR(64)  NOT NULL COMMENT '会话ID，关联 ai_sessions.session_id',
    summary_content TEXT         NOT NULL COMMENT '滚动摘要正文',
    covered_count   INT          NOT NULL DEFAULT 0 COMMENT '已纳入摘要的消息条数(user+assistant合计)',
    version         INT          NOT NULL DEFAULT 1 COMMENT '摘要版本号，每次滚动+1',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话滚动摘要';
