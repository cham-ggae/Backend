package com.example.demo.surveyResult.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.surveyResult.service.surveyService;
import com.example.demo.surveyResult.dto.surveyResultDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class surveyController {
        private final surveyService surveyService;

        @PostMapping("/surveyResult")
    public  ResponseEntity<?> surveyResult(@RequestBody surveyResultDto dto){
            surveyService.surveyResult(dto);
            return ResponseEntity.ok("설문 결과 저장 완료");
        }
}
