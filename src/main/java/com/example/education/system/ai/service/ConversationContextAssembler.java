package com.example.education.system.ai.service;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 对话上下文组装器 —— 摘要模式下，将多来源信息拼成发给大模型的 {@link Message} 列表。
 * <p>
 * <b>组装顺序（与 token 优先级一致）：</b>
 * <ol>
 *   <li>System Prompt（角色与行为约束）</li>
 *   <li>MySQL 滚动摘要（长期记忆，压缩后的历史）</li>
 *   <li>Redis 快照 + 热消息（尚未纳入摘要的近期对话）</li>
 *   <li>当前用户问题</li>
 * </ol>
 * <p>
 * 实现类：{@link com.example.education.system.ai.service.impl.ConversationContextAssemblerImpl}
 * <br>
 * 调用方：{@link com.example.education.system.ai.service.impl.ChatServiceImpl#chatWithRollingSummary}
 */
public interface ConversationContextAssembler {

    /**
     * 为指定会话组装完整上下文。
     *
     * @param sessionId 会话 ID（已在校验归属权之后传入）
     * @param question  当前用户问题（尚未写入 Redis，由本方法追加到列表末尾）
     * @return 可直接传给 {@code ChatClient.prompt().messages(...)} 的消息列表
     */
    List<Message> assemble(String sessionId, String question);
}
