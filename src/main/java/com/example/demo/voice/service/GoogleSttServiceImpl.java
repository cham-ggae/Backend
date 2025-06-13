package com.example.demo.voice.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
public class GoogleSttServiceImpl implements GoogleSttService {

    @Value("${google.credentials-path}")
    private String credentialsPath;

    private final ResourceLoader resourceLoader;

    public GoogleSttServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public String transcribe(byte[] audioBytes) {
        try {
            Resource resource = resourceLoader.getResource(credentialsPath);
            InputStream credentialsStream = resource.getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            SpeechSettings settings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            try (SpeechClient speechClient = SpeechClient.create(settings)) {

                RecognitionAudio audio = RecognitionAudio.newBuilder()
                        .setContent(ByteString.copyFrom(audioBytes))
                        .build();

                RecognitionConfig config = RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                        .setLanguageCode("ko-KR")
                        .build();

                RecognizeResponse response = speechClient.recognize(config, audio);

                StringBuilder transcript = new StringBuilder();
                for (SpeechRecognitionResult result : response.getResultsList()) {
                    transcript.append(result.getAlternativesList().get(0).getTranscript());
                }

                return transcript.toString();
            }
        } catch (Exception e) {
            log.error("STT 변환 실패", e);
            return "음성 인식에 실패했습니다.";
        }
    }
}
