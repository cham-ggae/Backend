package com.example.demo.surveyResult.dto;

import lombok.Data;

@Data
public class SurveyResponseDto {
    private int bugId;
    private int suggest1;
    private int suggest2;
    private String bugName;
    private String feature;
    private String personality;
}
