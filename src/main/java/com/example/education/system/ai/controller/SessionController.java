package com.example.education.system.ai.controller;


import com.example.education.system.ai.VO.SessionVO;
import com.example.education.system.ai.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping
    public SessionVO createSession(@RequestParam(value = "n",defaultValue = "3") Integer num){
        return  this.chatSessionService.createSession(num);
    }

    @GetMapping("/hot")
    public List<SessionVO.Example> getHot(@RequestParam(value = "n",defaultValue = "3") Integer num){
        return  this.chatSessionService.getHot(num);
    }
}
