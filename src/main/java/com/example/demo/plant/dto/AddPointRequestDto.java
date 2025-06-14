package com.example.demo.plant.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddPointRequestDto {
    private String activityType;  // 활동 타입만 전달 받음
}