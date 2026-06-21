package com.example.education.system.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.education.system.ai.VO.SessionVO;
import com.example.education.system.ai.model.ChatSession;

import java.util.List;
import java.util.Optional;



public interface ChatSessionService extends IService<ChatSession> {

    SessionVO createSession(Integer num);

    List<SessionVO.Example> getHot(Integer num);

    /**
     * 校验 sessionId 是否属于当前登录用户，防止越权读写他人会话。
     */
    boolean isOwnedByCurrentUser(String sessionId);

    Optional<ChatSession> findBySessionId(String sessionId);

    /**
     * 滑动续期：更新 expire_at，供 Redis TTL 刷新使用。
     */
    void touchSession(String sessionId);

    /**
     * 校验 session 未过期，过期则抛出异常。
     */
    void ensureSessionActive(String sessionId);
}
