package com.example.demo.familycard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 가족 메시지 카드 댓글 엔티티 DTO
 * Family_cards_comment 테이블과 매핑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyCardComment {

    /**
     * 댓글 고유 ID
     */
    private Long commentId;

    /**
     * 메시지 카드 ID (Foreign Key)
     */
    private Long fcid;

    /**
     * 댓글 작성자 사용자 ID (Foreign Key)
     */
    private Long uid;

    /**
     * 댓글 내용
     */
    private String content;

    /**
     * 댓글 작성일시
     */
    private LocalDateTime createdAt;
}