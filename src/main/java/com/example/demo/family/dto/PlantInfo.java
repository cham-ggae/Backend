package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 가족 스페이스용 간소화된 식물 정보 DTO
 * 필수 정보만 포함: 레벨, 종류, 생성 여부
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlantInfo {

    /**
     * 식물 존재 여부
     */
    private boolean hasPlant;

    /**
     * 식물 레벨 (식물이 있는 경우)
     */
    private Integer level;

    /**
     * 식물 종류 (식물이 있는 경우)
     * "flower" 또는 "tree"
     */
    private String plantType;

    /**
     * 새로운 식물 생성 가능 여부
     */
    private boolean canCreateNew;

    /**
     * 생성 불가능한 경우의 간단한 이유
     */
    private String createBlockReason;

    /**
     * 식물이 없는 상태 생성
     */
    public static PlantInfo noPlant(boolean canCreate, String reason) {
        return new PlantInfo(false, null, null, canCreate, reason);
    }

    /**
     * 식물이 있는 상태 생성
     */
    public static PlantInfo hasPlant(int level, String plantType, boolean canCreateNew) {
        return new PlantInfo(true, level, plantType, canCreateNew, null);
    }

    /**
     * 새 식물 생성 가능한 상태 (기존 식물 없음)
     */
    public static PlantInfo canCreateNew() {
        return new PlantInfo(false, null, null, true, null);
    }
}