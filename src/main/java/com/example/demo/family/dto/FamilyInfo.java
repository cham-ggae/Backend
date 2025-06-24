package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 가족 기본 정보 DTO
 * 초대 코드 검증 시 표시용 간소화된 가족 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyInfo {

    /**
     * 가족 스페이스 ID
     */
    private Long fid;

    /**
     * 가족 이름
     */
    private String name;

    /**
     * 현재 구성원 수
     */
    private Integer memberCount;

    /**
     * 결합 상품 타입
     */
    private String combiType;

    /**
     * 편의 메서드: 가족 참여 가능 여부
     */
    public boolean canJoin() {
        return memberCount != null && memberCount < 5;
    }

    /**
     * 편의 메서드: 남은 자리 수
     */
    public int getRemainingSlots() {
        return memberCount != null ? Math.max(0, 5 - memberCount) : 0;
    }
}