package com.example.demo.voice.controller;

import com.example.demo.voice.dto.TranscribedTextResponse;
import com.example.demo.voice.dto.TtsLogRequest;
import com.example.demo.voice.dto.TtsLogResponse;
import com.example.demo.voice.service.VoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/voice")
public class VoiceController {

    private final VoiceService voiceService;

    // 음성 파일 업로드 및 STT 처리
    @PostMapping("/upload")
    public ResponseEntity<TranscribedTextResponse> uploadAudio(
            @RequestParam("audio_file") MultipartFile audioFile,
            @RequestParam("session_id") Long sessionId
//            @RequestHeader("Authorization") String authHeader
    ) throws IOException {
//        String token = authHeader.replace("Bearer ", "");
        TranscribedTextResponse response = voiceService.handleAudioUpload(audioFile, sessionId, null);
        return ResponseEntity.ok(response);
    }


    // TTS 로그 저장
    @PostMapping("/tts-log")
    public ResponseEntity<TtsLogResponse> saveTtsLog(@RequestBody TtsLogRequest request) {
        return ResponseEntity.ok(voiceService.saveTtsLog(request));
    }
}