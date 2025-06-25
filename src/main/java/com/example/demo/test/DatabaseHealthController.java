package com.example.demo.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DatabaseHealthController {
    
    @Autowired
    private DataSource dataSource;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("profile", activeProfile);
        status.put("timestamp", LocalDateTime.now());
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            status.put("status", "UP");
            status.put("database", Map.of(
                "product", metaData.getDatabaseProductName(),
                "version", metaData.getDatabaseProductVersion(),
                "url", maskUrl(metaData.getURL())
            ));
            
            // 메모리 정보 (Render 모니터링용)
            if ("prod".equals(activeProfile)) {
                status.put("memory", getMemoryInfo());
            }
            
            return ResponseEntity.ok(status);
        } catch (SQLException e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            status.put("database", Map.of("connection", "FAILED"));
            
            return ResponseEntity.status(503).body(status);
        }
    }
    
    private String maskUrl(String url) {
        // 비밀번호 마스킹
        return url.replaceAll("password=[^&\\s]+", "password=***");
    }
    
    private Map<String, String> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        return Map.of(
            "used", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + "MB",
            "free", runtime.freeMemory() / 1024 / 1024 + "MB",
            "total", runtime.totalMemory() / 1024 / 1024 + "MB",
            "max", runtime.maxMemory() / 1024 / 1024 + "MB"
        );
    }
} 