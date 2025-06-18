package com.example.demo.chatbot.service;

import com.example.demo.chatbot.dto.Chatting;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface Chatbot {
    public void streamChatting(List<Map<String, String>> messages, String sessionId, Consumer<String> consumer) throws JsonProcessingException;
    public List<Chatting> getChattingList(String sessionId);
    public ResponseEntity<String> storeChatting(Chatting chatting);
}
