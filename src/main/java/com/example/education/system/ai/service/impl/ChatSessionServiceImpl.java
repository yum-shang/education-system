package com.example.education.system.ai.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.education.system.ai.VO.SessionVO;
import com.example.education.system.ai.config.SessionProperties;
import com.example.education.system.ai.model.ChatSession;
import com.example.education.system.ai.repository.ChatSessionMapper;
import com.example.education.system.ai.service.ChatSessionService;
import com.example.education.system.common.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    private final SessionProperties sessionProperties;

    @Override
    public SessionVO createSession(Integer num) {
        SessionVO sessionVO = new SessionVO();
        BeanUtils.copyProperties(this.sessionProperties, sessionVO);

        // 随机选取 num 个热门问题
        List<SessionVO.Example> shuffled = new ArrayList<>(this.sessionProperties.getExamples());
        Collections.shuffle(shuffled);
        sessionVO.setExamples(shuffled.subList(0, Math.min(num , shuffled.size())));

        // 生成 sessionId
        sessionVO.setSessionId(IdUtil.simpleUUID());

        // 保存会话数据到数据库
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionVO.getSessionId());
        chatSession.setUserId(UserContext.getUserId());
        chatSession.setCreateTime(new Timestamp(System.currentTimeMillis()));
        super.save(chatSession);

        return sessionVO;
    }

    @Override
    public List<SessionVO.Example> getHot(Integer num) {
        List<SessionVO.Example> shuffled = new ArrayList<>(this.sessionProperties.getExamples());
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(num, shuffled.size()));
    }

}
