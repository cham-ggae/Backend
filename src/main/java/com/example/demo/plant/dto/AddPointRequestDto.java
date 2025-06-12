package com.example.demo.plant.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddPointRequestDto {
    private Long uid;              // 사용자 ID
    private Long fid;              // 가족 ID
    private String activityType;  // 활동 타입
}
