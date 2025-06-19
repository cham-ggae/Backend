package com.example.demo.voice.controller;

import com.example.demo.voice.dto.TranscribedTextResponse;
import com.example.demo.voice.dto.TtsLogRequest;
import com.example.demo.voice.dto.TtsLogResponse;
import com.example.demo.voice.service.VoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    @Operation(summary = "음성 업로드", description = "음성 파일 업로드 및 STT 처리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TranscribedTextResponse> uploadAudio(
            @Parameter(description = "음성 파일", content = @Content(mediaType = "multipart/form-data"))
            @RequestParam("audio_file") MultipartFile audioFile,
            @Parameter(description = "세션 ID")
            @RequestParam("session_id") Long sessionId
//            @RequestHeader("Authorization") String authHeader
    ) throws IOException {
//        String token = authHeader.replace("Bearer ", "");
        TranscribedTextResponse response = voiceService.handleAudioUpload(audioFile, sessionId, null);
        return ResponseEntity.ok(response);
    }

    // TTS 변환 및 스트리밍, 저장
    @GetMapping(value = "/tts/{cid}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> streamTts(@PathVariable int cid) {
        byte[] audio = voiceService.getTtsAudioByCid(cid);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .body(audio);
    }
}