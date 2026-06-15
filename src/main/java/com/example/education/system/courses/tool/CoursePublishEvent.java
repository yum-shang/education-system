package com.example.education.system.courses.tool;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 课程推送事件 — 工具方法被 AI 调用时，通过此事件把课程数据推送给 SSE 流。
 * <p>
 * requestId 由 ChatServiceImpl 生成，通过 ToolContext 传入工具方法，
 * 用于区分不同请求，避免并发请求的事件错乱。
 */
@Data
@AllArgsConstructor
public class CoursePublishEvent {
    /** 单次请求 ID（UUID），关联 ToolContext 和 SSE 流 */
    private String requestId;
    /** 课程数据，格式：{@code {"courseInfo_xxx": {"id":"xxx", "name":"...", ...}}} */
    private Map<String, Object> courseData;
}
