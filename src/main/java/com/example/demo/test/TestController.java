package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        // 여기까지 진입했다면 (JWT 검증 통과 + 인증된 유저) 성공
        return "success";
    }
}
