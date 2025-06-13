package com.example.demo.voice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface GoogleSttService {
    String uploadFileToGCS(MultipartFile file) throws IOException;
    String transcribeAudio(String gcsUri) throws IOException;
}
