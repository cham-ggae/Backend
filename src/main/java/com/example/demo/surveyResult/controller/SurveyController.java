package com.example.demo.surveyResult.controller;

import com.example.demo.surveyResult.dto.SurveyResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.surveyResult.service.SurveyService;
import com.example.demo.surveyResult.dto.SurveyResultDto;

@RestController
@RequiredArgsConstructor
@Tag(name = "설문 결과", description = "설문 유형 결과 저장 API")
public class SurveyController {
        private final SurveyService surveyService;

        //설문 결과 저장
        @PostMapping("/surveyResult")
        @Operation(summary = "설문 유형 결과 저장", description = "설문 후 유형 결과 DB에 저장")
    public  ResponseEntity<?> surveyResult(@RequestBody SurveyResultDto dto){
            surveyService.SurveyResult(dto);
            return ResponseEntity.ok("설문 결과 저장 완료");
        }

        //bugId로 유형 상세 정보 조회
    @GetMapping("/surveyResult/{bugId}")
    @Operation(summary = "설문 유형 결과 조회", description = "설문 후 유형 결과 조회하기")
    public ResponseEntity<SurveyResponseDto> selectedBugId(@PathVariable int bugId){
            SurveyResponseDto result = surveyService.selectedBugId(bugId);
            return ResponseEntity.ok(result);
    }
}

