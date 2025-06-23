package com.example.demo.chatbot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RagSource {
    private String title;
    private String section;
    private String content;
}
