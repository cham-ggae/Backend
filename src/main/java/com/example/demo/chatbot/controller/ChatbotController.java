package com.example.demo.chatbot.controller;

import com.example.demo.chatbot.service.ChatbotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String prompt) throws JsonProcessingException {
        SseEmitter emitter = new SseEmitter();

        chatbotService.streamChatting(prompt, chunk -> {
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
}
