package com.example.demo.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@RequiredArgsConstructor
public class Chatting {
    private Long cid;
    private Long uid;
    private String role;
    private String content;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("session_id")
    private String sessionId;
}
