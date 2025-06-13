package com.example.demo.voice.service;

import com.example.demo.chatbot.service.ChatbotService;
import com.example.demo.login.dao.UserDao;
import com.example.demo.provider.JwtProvider;
import com.example.demo.voice.dao.VoiceDao;
import com.example.demo.voice.dto.TranscribedTextResponse;
import com.example.demo.voice.dto.TtsLogRequest;
import com.example.demo.voice.dto.TtsLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class VoiceServiceImpl implements VoiceService {

    private final VoiceDao voiceDao;
    private final ChatbotService chatbotService;
    private final JwtProvider jwtProvider;
    private final GoogleSttService googleSttService;
    private final UserDao userDao;

    @Override
    public TranscribedTextResponse handleAudioUpload(MultipartFile file, Long sessionId, String token) {
        try {
            // 1. 이메일 추출
//            String email = jwtProvider.getEmail(token);

            // 2. uid 조회
//            int uid = userDao.findByEmail(email).getUid().intValue();

            // 3. STT 처리
            byte[] audioBytes = file.getBytes(); // IOException 발생 가능
            String text = googleSttService.transcribe(audioBytes);

            // 4. 챗봇 연동 및 DB 저장
//            voiceDao.insertChat(uid, "USER", text, sessionId);
//            String botResponse = chatbotService.askOnce(text);
//            voiceDao.insertChat(uid, "BOT", botResponse, sessionId);

            return new TranscribedTextResponse(true,
                    new TranscribedTextResponse.Data(text, "msg_session_" + sessionId));

        } catch (IOException e) {
            throw new RuntimeException("음성 파일 처리 중 오류가 발생했습니다.", e);
//        } catch (SQLException e) {
//            throw new RuntimeException("사용자 정보를 조회하는 중 오류가 발생했습니다.", e);
        }
    }


    @Override
    public TtsLogResponse saveTtsLog(TtsLogRequest request) {
        voiceDao.insertTtsLog(request);
        return new TtsLogResponse(true, "로그 저장 완료");
    }
}
