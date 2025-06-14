package com.example.demo.plant.websocket;

import com.example.demo.plant.websocket.dto.WaterEventData;
import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class WaterWebSocketHandler extends TextWebSocketHandler {

    public static final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null || uri.getQuery() == null || !uri.getQuery().contains("token=")) {
                session.close(CloseStatus.BAD_DATA);
                return;
            }
            String token = uri.getQuery().split("token=")[1];
            Claims claims = JwtUtil.parseToken(token, jwtSecret);
            Long uid = claims.get("uid", Long.class);
            String name = claims.get("name", String.class);
            String avatarUrl = claims.get("profile_image", String.class);

            log.info("WebSocket 연결됨: {}, uid={}, name={}", session.getId(), uid, name);

            // 연결된 사용자 정보를 세션에 저장해도 좋음 (Optional)
            session.getAttributes().put("uid", uid);
            session.getAttributes().put("name", name);
            session.getAttributes().put("avatarUrl", avatarUrl);

            sessions.add(session);
        } catch (Exception e) {
            log.warn("JWT 파싱 실패 또는 연결 거부: {}", e.getMessage());
            try {
                session.close(CloseStatus.BAD_DATA);
            } catch (Exception ignored) {}
        }
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
