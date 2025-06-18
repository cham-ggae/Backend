package com.example.demo.mypage.controller;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.mypage.dto.MyPageResponse;
import com.example.demo.mypage.dto.RecommendHistoryData;
import com.example.demo.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Tag(name="My Page", description = "사용자 마이페이지 API")
/**
 * MyPageController 클래스입니다.
 */
public class MyPageController {

    private final MyPageService myPageService;
    private final AuthenticationService authenticationService;

    /**
     * 전체 마이페이지 정보 조회 API
     */
    @GetMapping
    @Operation(summary = "마이페이지 전체 정보", description = "기본정보 + 설문결과 + 추천요금제 히스토리 조회")

    public ResponseEntity<MyPageResponse> getMyPageInfo() throws NotFoundException {
        MyPageResponse response = myPageService.getMyPageInfo();
        return ResponseEntity.ok(response);
    }

    /**
     * 추천 히스토리 전용 API
     */
    @GetMapping("/history")
    @Operation(summary = "추천 요금제 히스토리", description = "과거 추천받은 요금제 조회")
    public ResponseEntity<List<RecommendHistoryData>> getHistory() {
        Long userId = authenticationService.getCurrentUserId();
        List<RecommendHistoryData> history = myPageService.getRecommendHistory(userId);
        return ResponseEntity.ok(history);
    }
}



