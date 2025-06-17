package com.example.demo.voice.service;

public interface GoogleTtsService {
    byte[] synthesizeSpeech(String text); // MP3 binary 반환
}
