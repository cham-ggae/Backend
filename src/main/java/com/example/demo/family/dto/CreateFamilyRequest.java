package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 가족 스페이스 생성 요청 DTO
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFamilyRequest {

    /**
     * 가족 이름 (최대 10자)
     */
    private String name;

    /**
     * 결합 상품 타입
     * 예: "투게더 결합", "참쉬운 가족 결합" 등
     */
    private String combiType;
}