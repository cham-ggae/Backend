package com.example.demo.voice.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class GoogleSttServiceImpl implements GoogleSttService {

    @Value("${google.credentials-path}")
    private String credentialsPath;

    @Value("${google.bucket-name}")
    private String bucketName;

    private final ResourceLoader resourceLoader;

    public GoogleSttServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    // ✅ GCS에 업로드하고 URI 반환
    @Override
    public String uploadFileToGCS(MultipartFile file) {
        try {
            Resource resource = resourceLoader.getResource(credentialsPath);
            InputStream credentialsStream = resource.getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();

            String fileName = "audio/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            return String.format("gs://%s/%s", bucketName, fileName);
        } catch (Exception e) {
            log.error("GCS 파일 업로드 실패", e);
            throw new RuntimeException("GCS 파일 업로드 실패");
        }
    }

    // ✅ GCS URI로 STT 실행
    @Override
    public String transcribeAudio(String gcsUri) {
        try {
            Resource resource = resourceLoader.getResource(credentialsPath);
            InputStream credentialsStream = resource.getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            SpeechSettings settings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            try (SpeechClient speechClient = SpeechClient.create(settings)) {
                RecognitionConfig config = RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                        .setLanguageCode("ko-KR")
                        .build();

                RecognitionAudio audio = RecognitionAudio.newBuilder()
                        .setUri(gcsUri)
                        .build();

                RecognizeResponse response = speechClient.recognize(config, audio);

                StringBuilder transcript = new StringBuilder();
                for (SpeechRecognitionResult result : response.getResultsList()) {
                    transcript.append(result.getAlternativesList().get(0).getTranscript());
                }

                return transcript.toString();
            }
        } catch (Exception e) {
            log.error("GCS 기반 STT 변환 실패", e);
            return "음성 인식에 실패했습니다.";
        }
    }
}
