package com.example.demo.chatbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public interface Chatbot {
    public void streamChatting(String prompt, Consumer<String> consumer) throws JsonProcessingException;
}
