package com.example.demo.plant.websocket;

import com.example.demo.plant.websocket.dto.PlantEventData;
import com.example.demo.provider.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class PlantWebSocketHandler extends TextWebSocketHandler {

    public static final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtProvider jwtProvider;

    public PlantWebSocketHandler(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        String token = null;

        if (query != null && query.startsWith("token=")) {
            token = query.substring("token=".length());
        }

        if (token == null || !jwtProvider.validateToken(token)) {
            log.warn("WebSocket 연결 거부 - 유효하지 않은 토큰");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        String email = jwtProvider.getEmail(token);
        session.getAttributes().put("userEmail", email);
        sessions.add(session);
        log.info("WebSocket 연결 성공 - 사용자: {}", email);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket 연결 종료: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("식물 이벤트 수신: {}", payload);

        PlantEventData data = objectMapper.readValue(payload, PlantEventData.class);
        log.info("받은 이벤트: type={}, fid={}, uid={}, name={}", data.getType(), data.getFid(), data.getUid(), data.getName());

        String broadcast = objectMapper.writeValueAsString(data);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(broadcast));
            }
        }
    }
}
