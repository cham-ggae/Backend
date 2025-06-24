package com.example.demo.gcs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class GcsDownloader {

    @Value("${google.credentials-path}")
    private String credentialsPath;

    private final ResourceLoader resourceLoader;

    public GcsDownloader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public byte[] download(String gcsUrl) {
        try {
            String bucketName;
            String objectName;

            if (gcsUrl.startsWith("gs://")) {
                // gs:// 형식 처리
                String[] parts = gcsUrl.replace("gs://", "").split("/", 2);
                bucketName = parts[0];
                objectName = parts[1];
            } else if (gcsUrl.startsWith("https://storage.googleapis.com/")) {
                // https:// 형식 처리
                String path = gcsUrl.replace("https://storage.googleapis.com/", "");
                String[] parts = path.split("/", 2);
                bucketName = parts[0];
                objectName = parts[1];
            } else {
                throw new IllegalArgumentException("지원하지 않는 GCS URL 형식입니다: " + gcsUrl);
            }

            InputStream credentialsStream = resourceLoader.getResource(credentialsPath).getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();

            Blob blob = storage.get(bucketName, objectName);
            if (blob == null) {
                throw new RuntimeException("GCS 객체를 찾을 수 없습니다: " + objectName);
            }
            byte[] content = blob.getContent();
            System.out.println("mp3 길이 : "+content.length);

            return content;

        } catch (Exception e) {
            throw new RuntimeException("GCS 다운로드 실패", e);
        }
    }

}
