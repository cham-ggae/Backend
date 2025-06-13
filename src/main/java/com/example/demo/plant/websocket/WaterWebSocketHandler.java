package com.example.demo.plant.websocket;

import com.example.demo.plant.websocket.dto.WaterEventData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class WaterWebSocketHandler extends TextWebSocketHandler {

    public static final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket 연결됨: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket 연결 종료: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("물주기 이벤트 수신 : {}", payload);

        WaterEventData data = objectMapper.readValue(payload, WaterEventData.class);
        log.info("받은 물주기 이벤트: fid={}, uid={}, name={}", data.getFid(), data.getUid(), data.getName());

        String broadcast = objectMapper.writeValueAsString(data);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(broadcast));
            }
        }
    }
}