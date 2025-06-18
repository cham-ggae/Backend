package com.example.demo.chatbot.service;

import com.example.demo.chatbot.dao.ChatbotDao;
import com.example.demo.chatbot.dto.ChatCompletionChunk;
import com.example.demo.chatbot.dto.Chatting;
import com.example.demo.login.service.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService implements Chatbot{
    private final OkHttpClient client = new OkHttpClient();
    private final ChatbotDao chatbotDao;

    @Value("${openai.key}")
    private String OPENAI_API_KEY;
    @Value("${openai.model}")
    private String MODEL;

    private final AuthenticationService authenticationService;
    @Override
    public void streamChatting(List<Map<String, String>> messages, String sessionId, Consumer<String> consumer) throws JsonProcessingException {
        Long userId = authenticationService.getCurrentUserId();
        String userContent = messages.get(messages.size() - 1).get("content");
        Chatting userChat = new Chatting();
        userChat.setUid(userId);
        userChat.setRole("user");
        userChat.setContent(userContent);
        userChat.setSessionId(sessionId);
        try {
            chatbotDao.insertChatting(userChat);
        } catch (Exception e) {
            log.warn("유저 채팅 저장 실패: {}", e.getMessage());
        }
        System.out.println(messages);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", MODEL);
        payload.put("stream", true);
        payload.put("messages", messages);

        String jsonBody = mapper.writeValueAsString(payload);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .build();
        StringBuilder aiBuilder = new StringBuilder();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                consumer.accept("[ERROR] " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                StringBuilder aiBuilder = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body().byteStream()))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("data: ")) continue;
                        String json = line.substring(6).trim();

                        // JSON 파싱
                        ChatCompletionChunk chunk = mapper.readValue(json, ChatCompletionChunk.class);
                        var choice = chunk.getChoices().get(0);

                        // 1) 델타가 오면 누적하고 즉시 클라이언트로 전송
                        String delta = choice.getDelta().getContent();
                        if (delta != null && !delta.isBlank()) {
                            aiBuilder.append(delta);
                            consumer.accept(json);
                        }

                        // 2) finish_reason 이 나오면 스트림 종료 플래그
                        if (choice.getFinishReason() != null) {
                            consumer.accept(json);
                            break;
                        }
                    }

                } catch (Exception e) {
                    consumer.accept("[ERROR] " + e.getMessage());
                }
                Chatting aiChat = new Chatting();
                aiChat.setUid(userId);
                aiChat.setRole("assistant");
                aiChat.setContent(aiBuilder.toString());
                aiChat.setSessionId(sessionId);
                try {
                    chatbotDao.insertChatting(aiChat);
                } catch (Exception e) {
                    log.warn("AI 채팅 저장 실패: {}", e.getMessage());
                }
            }
        });
    }

    @Override
    public List<Chatting> getChattingList(String sessionId) {
        // 1) 파라미터 검증
        if (!StringUtils.hasText(sessionId)) {
            return Collections.emptyList();
        }
        try {
            List<Chatting> chats = chatbotDao.selectChatting(sessionId);
            // 2) 결과 없음 → 빈 리스트 반환
            if (chats == null || chats.isEmpty()) {
                return Collections.emptyList();
            }
            // 3) 정상 처리
            return chats;

        } catch (Exception e) {
            // 4) 예외 처리: 로깅 후 빈 리스트 반환
            log.error("채팅 조회 실패. sessionId={}, error={}", sessionId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public ResponseEntity<String> storeChatting(Chatting chatting) {
        try {
            chatbotDao.insertChatting(chatting);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("저장 실패: " + e.getMessage());
        }
    }
}
