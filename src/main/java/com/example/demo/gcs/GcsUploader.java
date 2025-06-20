package com.example.demo.gcs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

@Component
@RequiredArgsConstructor
public class GcsUploader {

    private final ResourceLoader resourceLoader;

    @Value("${google.credentials-path}")
    private String credentialsPath;

    @Value("${google.bucket-name}")
    private String bucketName;

    private Storage storage;

    private void initStorage() throws Exception {
        if (storage == null) {
            InputStream credentialsStream = resourceLoader.getResource(credentialsPath).getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        }
    }

    public String upload(byte[] data, String objectName) {
        try {
            initStorage();
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("audio/mpeg").build();
            storage.create(blobInfo, data);
            return String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
        } catch (Exception e) {
            throw new RuntimeException("GCS 업로드 실패", e);
        }
    }

    // 파일 업로드 메서드
    public String upload(File file, String objectName) {
        try {
            initStorage();
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("audio/wav").build();
            storage.create(blobInfo, Files.readAllBytes(file.toPath()));
            return String.format("gs://%s/%s", bucketName, objectName);
        } catch (Exception e) {
            throw new RuntimeException("GCS 업로드 실패", e);
        }
    }

}
