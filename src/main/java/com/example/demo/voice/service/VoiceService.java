package com.example.demo.voice.service;

import com.example.demo.voice.dto.TranscribedTextResponse;
import com.example.demo.voice.dto.TtsLogRequest;
import com.example.demo.voice.dto.TtsLogResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VoiceService {
    // 음성 업로드 처리 및 STT → 챗봇 응답 처리
    TranscribedTextResponse handleAudioUpload(MultipartFile file, String sessionId, String token);

    // 텍스트 -> 음성 변환 + 저장
    TtsLogResponse convertAndLogTts(int cid, String text);

    // 변환된 음성(mp3) 응답
    byte[] getTtsAudioByCid(int cid);
}
