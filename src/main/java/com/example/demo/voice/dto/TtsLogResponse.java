package com.example.demo.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TtsLogResponse {
    private boolean success; // 요청 성공 여부
    private String message;  // 결과 메시지
}
