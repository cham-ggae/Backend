package com.example.demo.plant.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 식물 생성 요청 DTO
 * 사용자가 선택한 식물 종류 (flower/tree)를 담는다
 */
@Getter
@Setter
public class CreatePlantRequestDto {
    private String plantType;
}
