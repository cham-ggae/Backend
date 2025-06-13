package com.example.demo.voice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TtsLogRequest {
    private int cid;         // chats.cid
    private String ttsUrl;   // 변환된 음성(mp3) URL
}
