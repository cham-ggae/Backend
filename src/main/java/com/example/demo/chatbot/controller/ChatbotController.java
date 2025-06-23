package com.example.demo.chatbot.controller;

import com.example.demo.chatbot.dao.ChatbotDao;
import com.example.demo.chatbot.dto.Chatting;
import com.example.demo.chatbot.service.ChatbotService;
import com.example.demo.login.service.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatbotController {
    private final ChatbotService chatbotService;
    private final AuthenticationService authenticationService;
    private final ChatbotDao chatbotDao;
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String prompt, @RequestParam String sessionId, @RequestParam Long members) throws JsonProcessingException {
        SseEmitter emitter = new SseEmitter();
        List<Chatting> history = chatbotDao.selectChatting(sessionId);
        Collections.reverse(history);
        List<Map<String, String>> messages = new ArrayList<>();
        for (Chatting c : history) {
            String role = c.getRole().equals("bot") ? "assistant" : c.getRole();
            messages.add(Map.of("role", role, "content", c.getContent()));
        }
        messages.add(Map.of("role", "user", "content", prompt));
        chatbotService.streamChatting(messages, sessionId, members, chunk -> {
            try {
                emitter.send(chunk);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // 선택적으로 타임아웃 핸들링
        emitter.onTimeout(emitter::complete);

        return emitter;
    }
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> storeChat(@RequestBody Chatting chatting) {
        chatting.setUid(authenticationService.getCurrentUserId());
        return chatbotService.storeChatting(chatting);
    }

    // 3) 세션별 채팅 내역 조회 (최신 20개 등 DAO 구현에 따라)
    @GetMapping("/history")
    public ResponseEntity<List<Chatting>> getChattingList(@RequestParam String sessionId) {
        List<Chatting> list = chatbotService.getChattingList(sessionId);
        return ResponseEntity.ok(list);
    }
}
