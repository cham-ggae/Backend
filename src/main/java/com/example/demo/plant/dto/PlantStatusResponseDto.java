package com.example.demo.plant.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 식물 상태 응답 DTO
 * - 레벨, 경험치, 완료 여부, 식물 타입 포함
 */
@Getter
@Setter
public class PlantStatusResponseDto {
    private int level;
    private int experiencePoint;
    private int expThreshold; // 💡 추가 필드
    private boolean isCompleted;
    private String plantType;
}