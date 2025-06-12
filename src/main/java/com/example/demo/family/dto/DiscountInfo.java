package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 가족 결합 할인 정보 DTO
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountInfo {

    /**
     * 총 월 할인 금액 (원)
     * 예: 52000
     */
    private Integer totalMonthly;

    /**
     * 할인 설명 문구
     * 예: "투게더 결합 이용 시 한달에 최대 52,000원 아낄 수 있어요!"
     */
    private String description;

    /**
     * 할인 계산 기준 구성원 수
     */
    private Integer memberCount;
}