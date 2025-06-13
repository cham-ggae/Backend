package com.example.demo.surveyResult.service;

import com.example.demo.surveyResult.dto.SurveyResultDto;
import com.example.demo.surveyResult.mapper.SurveyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SurveyService {
    private final SurveyMapper surveyMapper;

    public void SurveyResult(SurveyResultDto dto){
        surveyMapper.surveyResult(dto.getUserId(), dto.getBugId());
    }
}
