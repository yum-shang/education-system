package com.example.education.system.ai.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.education.system.ai.VO.SessionVO;
import com.example.education.system.ai.config.AiChatProperties;
import com.example.education.system.ai.config.SessionProperties;
import com.example.education.system.ai.model.ChatSession;
import com.example.education.system.ai.repository.ChatSessionMapper;
import com.example.education.system.ai.service.ChatSessionService;
import com.example.education.system.common.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    private final SessionProperties sessionProperties;
    private final AiChatProperties aiChatProperties;

    @Override
    public SessionVO createSession(Integer num) {
        SessionVO sessionVO = new SessionVO();
        BeanUtils.copyProperties(this.sessionProperties, sessionVO);

        List<SessionVO.Example> examples = this.sessionProperties.getExamples();
        if (examples == null || examples.isEmpty()) {
            sessionVO.setExamples(List.of());
        } else {
            List<SessionVO.Example> shuffled = new ArrayList<>(examples);
            Collections.shuffle(shuffled);
            int safeNum = num == null ? 0 : Math.max(0, num);
            sessionVO.setExamples(shuffled.subList(0, Math.min(safeNum, shuffled.size())));
        }

        sessionVO.setSessionId(IdUtil.simpleUUID());

        Timestamp now = new Timestamp(System.currentTimeMillis());
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionVO.getSessionId());
        chatSession.setUserId(UserContext.getUserId());
        chatSession.setCreateTime(now);
        chatSession.setUpdateTime(now);
        chatSession.setStatus("active");
        chatSession.setExpireAt(Timestamp.from(Instant.now().plus(aiChatProperties.getSessionTtl())));
        super.save(chatSession);

        return sessionVO;
    }

    @Override
    public List<SessionVO.Example> getHot(Integer num) {
        List<SessionVO.Example> examples = this.sessionProperties.getExamples();
        if (examples == null || examples.isEmpty()) {
            return List.of();
        }
        List<SessionVO.Example> shuffled = new ArrayList<>(examples);
        Collections.shuffle(shuffled);
        int safeNum = num == null ? 0 : Math.max(0, num);
        return shuffled.subList(0, Math.min(safeNum, shuffled.size()));
    }

    @Override
    public boolean isOwnedByCurrentUser(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            return false;
        }
        return lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, userId)
                .exists();
    }

    @Override
    public Optional<ChatSession> findBySessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .one());
    }

    @Override
    public void touchSession(String sessionId) {
        ChatSession session = findBySessionId(sessionId).orElse(null);
        if (session == null) {
            return;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        session.setUpdateTime(now);
        session.setExpireAt(Timestamp.from(Instant.now().plus(aiChatProperties.getSessionTtl())));
        if (session.getStatus() == null || session.getStatus().isBlank()) {
            session.setStatus("active");
        }
        updateById(session);
    }

    @Override
    public void ensureSessionActive(String sessionId) {
        ChatSession session = findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "会话不存在"));
        if (session.getExpireAt() != null && session.getExpireAt().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ResponseStatusException(HttpStatus.GONE, "会话已过期，请重新创建");
        }
        if ("closed".equals(session.getStatus()) || "expired".equals(session.getStatus())) {
            throw new ResponseStatusException(HttpStatus.GONE, "会话已关闭");
        }
    }
}
