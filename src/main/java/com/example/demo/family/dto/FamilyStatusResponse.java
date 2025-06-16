package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 사용자의 가족 상태 응답 DTO
 * 프론트엔드 UI 분기 처리에 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyStatusResponse {

    /**
     * 가족 가입 여부
     */
    private boolean hasFamily;

    /**
     * 가족 ID (가입된 경우)
     */
    private Long familyId;

    /**
     * 가족 이름 (가입된 경우)
     */
    private String familyName;

    /**
     * 편의 생성자 - 가족 미가입
     */
    public static FamilyStatusResponse noFamily() {
        return new FamilyStatusResponse(false, null, null);
    }

    /**
     * 편의 생성자 - 가족 가입
     */
    public static FamilyStatusResponse hasFamily(Long familyId, String familyName) {
        return new FamilyStatusResponse(true, familyId, familyName);
    }
}