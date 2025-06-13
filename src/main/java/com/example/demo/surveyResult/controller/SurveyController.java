package com.example.demo.surveyResult.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.surveyResult.service.SurveyService;
import com.example.demo.surveyResult.dto.SurveyResultDto;

@RestController
@RequiredArgsConstructor
@Tag(name = "설문 결과", description = "설문 유형 결과 저장 API")
public class SurveyController {
        private final SurveyService surveyService;

        @PostMapping("/surveyResult")
        @Operation(summary = "설문 유형 결과 저장", description = "설문 후 유형 결과 DB에 저장")
    public  ResponseEntity<?> surveyResult(@RequestBody SurveyResultDto dto){
            surveyService.SurveyResult(dto);
            return ResponseEntity.ok("설문 결과 저장 완료");
        }
}
