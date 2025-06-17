package com.example.demo.mypage.controller;

import com.example.demo.mypage.dto.MyPageResponse;
import com.example.demo.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Tag(name="My Page", description = "사용자 마이페이지 API")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/info")
    @Operation(summary = "사용자 기본 정보 조회", description = "사용자의 이름, 가입일, 프로필 이미지 조회")
    public ResponseEntity<MyPageResponse> getMyPage(){
        MyPageResponse response = myPageService.getMyPageInfo();
        return ResponseEntity.ok(response);
    }
}
