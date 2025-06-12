package com.example.demo.plant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantResponseDto {
    private int pid;
    private int fid;
    private String type;           // flower / tree
    private int level;             // 현재 레벨
    private int exp;               // 현재 누적 경험치
    private boolean isCompleted;   // 성장 완료 여부
    private String createdAt;      // 생성일시
}