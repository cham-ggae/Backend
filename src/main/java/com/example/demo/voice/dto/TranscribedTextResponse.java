package com.example.demo.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TranscribedTextResponse {
    private boolean success; // 요청 성공 여부
    private Data data;       // 변환된 텍스트 및 기타 정보

    @Getter
    @AllArgsConstructor
    public static class Data {
        private String transcribedText; // 변환된 텍스트
        private String messageId;       // 저장된 메시지 식별자
    }
}