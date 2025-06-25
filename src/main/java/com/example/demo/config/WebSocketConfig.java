package com.example.demo.config;

import com.example.demo.plant.websocket.PlantWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final PlantWebSocketHandler plantWebSocketHandler;

    @Autowired
    private Environment environment;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        
        if (isProd) {
            registry.addHandler(plantWebSocketHandler, "/ws/plant")
                    .setAllowedOrigins("https://modi-backend-th1n.onrender.com");
        } else {
            registry.addHandler(plantWebSocketHandler, "/ws/plant")
                    .setAllowedOrigins("http://localhost:3000", "https://localhost:3000");
        }
    }
}