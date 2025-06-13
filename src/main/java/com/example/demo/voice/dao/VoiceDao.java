package com.example.demo.voice.dao;

import com.example.demo.voice.dto.TtsLogRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VoiceDao {
    // 채팅 메시지 저장 (USER/BOT)
    void insertChat(@Param("uid") int uid, @Param("role") String role,
                    @Param("content") String content, @Param("sessionId") Long sessionId);

    // TTS 로그 저장
    void insertTtsLog(TtsLogRequest request);
}