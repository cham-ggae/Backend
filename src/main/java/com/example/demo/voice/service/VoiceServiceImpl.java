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
    private final GoogleTtsService googleTtsService;

    /*
    음성 파일 업로드 및 STT 처리 -> 텍스트 추출 후 챗봇 응답 요청까지
     */
    @Override
    public TranscribedTextResponse handleAudioUpload(MultipartFile file, Long sessionId, String token) {
        try {
            // 1. 이메일 추출
//            String email = jwtProvider.getEmail(token);

            // 2. uid 조회
//            int uid = userDao.findByEmail(email).getUid().intValue();

            // 3. 파일을 GCS에 업로드하고 URI 얻기
            String gcsUri = googleSttService.uploadFileToGCS(file);

            // 4. GCS 기반 STT 처리 → 텍스트 반환
            String text = googleSttService.transcribeAudio(gcsUri);


            // 5. 챗봇에 질문 전달 (응답 db에 저장하는건 아직 구현 안함)
            chatbotService.streamChatting(text, partial -> {
                // 응답 스트리밍 콜백 – 지금은 무시하거나, log만 찍기
                System.out.println("[챗봇 응답 일부]: " + partial);
            });

            // 6. 사용자 질문만 응답에 담아 리턴
            return new TranscribedTextResponse(true,
                    new TranscribedTextResponse.Data(text, "msg_session_" + sessionId));
        } catch (IOException e) {
            throw new RuntimeException("음성 파일 처리 중 오류가 발생했습니다.", e);
//        } catch (SQLException e) {
//            throw new RuntimeException("사용자 정보를 조회하는 중 오류가 발생했습니다.", e);
        }
    }

    /*
    텍스트를 음성 (MP3 바이트)로 변환
     */
    @Override
    public byte[] synthesizeSpeech(String text) {
        return googleTtsService.synthesizeSpeech(text);
    }

    /*
    TTS 로그를 DB에 저장
     */
    @Override
    public TtsLogResponse saveTtsLog(TtsLogRequest request) {
        voiceDao.insertTtsLog(request);
        return new TtsLogResponse(true, "로그 저장 완료");
    }
}
