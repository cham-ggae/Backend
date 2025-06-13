package com.example.demo.surveyResult.service;

import com.example.demo.surveyResult.dto.surveyResultDto;
import com.example.demo.surveyResult.mapper.surveyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class surveyService {
    private final surveyMapper surveyMapper;

    public void surveyResult(surveyResultDto dto){
        surveyMapper.surveyResult(dto.getUserId(), dto.getBugId());
    }
}
