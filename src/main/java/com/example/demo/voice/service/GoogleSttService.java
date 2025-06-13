package com.example.demo.voice.service;

public interface GoogleSttService {
    String transcribe(byte[] audioBytes);
}
