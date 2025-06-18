package com.example.demo.chatbot.dto;

import lombok.*;

@Data
@RequiredArgsConstructor
public class Chatting {
    private Long cid;
    private Long uid;
    private String role;
    private String content;
    private String created_at;
    private String session_id;
}
