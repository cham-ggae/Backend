package com.example.demo.voice.service;

import com.example.demo.voice.dto.TranscribedTextResponse;
import com.example.demo.voice.dto.TtsLogRequest;
import com.example.demo.voice.dto.TtsLogResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VoiceService {
    // 음성 업로드 처리 및 STT → 챗봇 응답 처리
    TranscribedTextResponse handleAudioUpload(MultipartFile file, Long sessionId, String token);

    // 텍스트를 음성으로 변환하여 byte 배열(mp3)을 반환
    byte[] synthesizeSpeech(String text);

    // TTS 로그 저장
    TtsLogResponse saveTtsLog(TtsLogRequest request);
}
