package com.example.demo.voice.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

@Slf4j
@Service
public class GoogleTtsServiceImpl implements GoogleTtsService {

    @Value("${google.credentials-path}")
    private String credentialsPath;

    private final ResourceLoader resourceLoader;

    public GoogleTtsServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 입력된 텍스트를 Google Cloud Text-to-Speech API를 이용해 음성 데이터(MP3)로 변환
     *
     * @param text 변환할 텍스트
     * @return 음성 데이터 (MP3 형식의 byte 배열)
     */
    @Override
    public byte[] synthesizeSpeech(String text) {
        try {
            // 1. 서비스 계정 키 파일을 classpath에서 불러오기
            Resource resource = resourceLoader.getResource(credentialsPath);
            InputStream credentialsStream = resource.getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            // 2. 자격 증명을 포함한 TTS 클라이언트 설정 구성
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(()  -> credentials)
                    .build();

            // 3. TTS 클라이언트 생성 및 사용
            try (TextToSpeechClient client = TextToSpeechClient.create(settings)) {
                // 4. 변환할 텍스트 설정
                SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

                // 5. 사용할 목소리 설정 (한국어, 중립적 음성)
                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                        .setLanguageCode("ko-KR")
                        .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                        .build();
                // 6. 출력 오디오 형식 설정 (MP3)
                AudioConfig audioConfig = AudioConfig.newBuilder()
                        .setAudioEncoding(AudioEncoding.MP3)
                        .build();

                // 7. TTS API 호출하여 음성 데이터 생성
                SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);
                // 8. 응답에서 음성 데이터(byte[]) 추출하여 반환
                return response.getAudioContent().toByteArray();
            }
        } catch (Exception e) {
            log.error("TTS 변환 실패", e);
            throw new RuntimeException("TTS 변환 실패", e);
        }
    }


}
