package com.example.demo.familycard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 가족 메시지 카드 목록 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyCardListResponse {

    /**
     * 메시지 카드 목록
     */
    private List<FamilyCardResponse> cards;

    /**
     * 총 카드 개수
     */
    private Integer totalCount;

    /**
     * 가족 스페이스 ID
     */
    private Long familyId;

    /**
     * 가족 스페이스 이름
     */
    private String familyName;
}