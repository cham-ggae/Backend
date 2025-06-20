package com.example.demo.surveyResult.controller;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.surveyResult.dto.SurveyResponseDto;
import com.example.demo.surveyResult.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 설문 결과 관련 요청을 처리하는 컨트롤러입니다.
 * - 설문 결과 저장 (인증 기반 사용자)
 * - 설문 결과 조회 (userId 필요 없음)
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "설문 결과", description = "설문 유형 결과 저장 API")
public class SurveyController {

    private final SurveyService surveyService;
    private final AuthenticationService authenticationService;

    /**
     * 인증된 사용자의 설문 결과를 저장합니다.
     * @param bugId 설문 결과 유형 ID (form-data 방식으로 전달)
     * @return 저장 성공 메시지
     */
    @PostMapping(
        value = "/surveyResult",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "설문 유형 결과 저장", description = "설문 후 유형 결과 DB에 저장")
    public ResponseEntity<?> surveyResult(@RequestParam("bugId") int bugId) {
        Long currentUserId = authenticationService.getCurrentUserId(); // 현재 로그인 사용자 ID 조회
        surveyService.surveyResult(currentUserId.intValue(), bugId); // ID 직접 주입
        return ResponseEntity.ok("설문 결과 저장 완료");
    }

    /**
     * bugId를 통해 설문 유형 결과를 조회합니다.
     * userId 불필요 (공개 API)
     *
     * @param bugId 유형 ID
     * @return 유형 상세 정보
     */
    @GetMapping("/surveyResult/{bugId}")
    @Operation(summary = "설문 유형 결과 조회", description = "설문 후 유형 결과 조회하기")
    public ResponseEntity<SurveyResponseDto> selectedBugId(@PathVariable int bugId) {
        SurveyResponseDto result = surveyService.selectedBugId(bugId);
        return ResponseEntity.ok(result);
    }
}
