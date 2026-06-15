package com.example.education.system.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.education.system.ai.VO.SessionVO;
import com.example.education.system.ai.model.ChatSession;

import java.util.List;



public interface ChatSessionService extends IService<ChatSession> {

    SessionVO createSession(Integer num);

    List<SessionVO.Example> getHot(Integer num);
}
