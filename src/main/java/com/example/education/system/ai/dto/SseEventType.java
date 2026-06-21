package com.example.education.system.ai.dto;

/**
 * SSE 事件类型常量枚举。
 * <p>
 * 前端根据 eventType 区分：文本流(1001)、完成(1002)、课程卡片(1003)。
 */
public enum SseEventType {

    /** AI 流式文本片段 */
    TEXT(1001),
    /** 对话结束（含正常完成与错误提示） */
    COMPLETE(1002),
    /** 工具查询到的课程卡片数据 */
    COURSE(1003);

    private final int code;

    SseEventType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
