package com.example.demo.familycard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 가족 메시지 카드 엔티티 DTO
 * Family_cards 테이블과 매핑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyCard {

    /**
     * 카드 고유 ID
     */
    private Long fcid;

    /**
     * 작성자 사용자 ID
     */
    private Long uid;

    /**
     * 이미지 타입 코드
     */
    private String image;

    /**
     * 메시지 내용
     */
    private String content;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;
}