package com.example.demo.chatbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class ChatbotService implements Chatbot{
    private final OkHttpClient client = new OkHttpClient();

    @Value("${openai.key}")
    private String OPENAI_API_KEY;
    @Value("${openai.model}")
    private String MODEL;
    @Override
    public void streamChatting(String prompt, Consumer<String> consumer) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", MODEL);
        payload.put("stream", true);
        payload.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        String jsonBody = mapper.writeValueAsString(payload);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                consumer.accept("[ERROR] " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String json = line.substring(6).trim();
                            if (!json.equals("[DONE]")) {
                                consumer.accept(json);
                            }
                        }
                    }
                } catch (Exception e) {
                    consumer.accept("[ERROR] " + e.getMessage());
                }
            }
        });
    }
}
