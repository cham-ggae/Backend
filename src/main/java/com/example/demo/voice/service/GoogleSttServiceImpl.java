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

import java.io.*;
import java.nio.file.Files;
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

    // GCS에 업로드하고 URI 반환
    @Override
    public String uploadFileToGCS(MultipartFile file) {
        try {
            File wavFile = convertToLinear16Wav(file);

            Resource resource = resourceLoader.getResource(credentialsPath);
            InputStream credentialsStream = resource.getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();

            String fileName = "audio/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                    .setContentType("audio/wav")
                    .build();

            storage.create(blobInfo, Files.readAllBytes(wavFile.toPath()));

            return String.format("gs://%s/%s", bucketName, fileName);
        } catch (Exception e) {
            log.error("GCS 파일 업로드 실패", e);
            throw new RuntimeException("GCS 파일 업로드 실패");
        }
    }

    // 파일 형식 자동으로 변환 -> .wav (LINEAR16, 16kHz)
    private File convertToLinear16Wav(MultipartFile inputFile) throws IOException {
        // 1. 원본 파일 임시로 저장
        File tempInput = File.createTempFile("input", "-" + inputFile.getOriginalFilename());
        inputFile.transferTo(tempInput);
        // 2. 변환된 .wav파일 임시 생성
        File tempWav = File.createTempFile("converted", ".wav");
        // 3. ffempeg 명령 구성
        String[] command = {
                "ffmpeg", "-y",
                "-i", tempInput.getAbsolutePath(),
                "-acodec", "pcm_s16le",
                "-ac", "1",
                "-ar", "16000",
                tempWav.getAbsolutePath()
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[ffmpeg] " + line);
            }
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("ffmpeg 변환 중 인터럽트 발생", e);
        }

        if (exitCode != 0) {
            throw new RuntimeException("ffmpeg 변환 실패. 종료 코드: " + exitCode);
        }

        return tempWav;
    }

    // GCS URI로 STT 실행
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
                        .setSampleRateHertz(16000)
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
