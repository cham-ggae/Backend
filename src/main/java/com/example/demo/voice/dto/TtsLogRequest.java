package com.example.demo.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TtsLogRequest {
    private int cid;         // chats.cid
    private String ttsUrl;   // 변환된 음성(mp3) URL
}
