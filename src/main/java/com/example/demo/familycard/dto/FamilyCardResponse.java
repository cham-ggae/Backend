package com.example.demo.familycard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 가족 메시지 카드 응답 DTO
 * 클라이언트에게 전달되는 카드 정보 + 작성자 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyCardResponse {

    /**
     * 카드 고유 ID
     */
    private Long fcid;

    /**
     * 작성자 사용자 ID
     */
    private Long uid;

    /**
     * 작성자 이름
     */
    private String authorName;

    /**
     * 작성자 프로필 이미지 URL
     */
    private String authorProfileImage;

    /**
     * 이미지 타입 코드
     */
    private String imageType;

    /**
     * 이미지 타입 설명
     */
    private String imageDescription;

    /**
     * 메시지 내용
     */
    private String content;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 현재 사용자가 이 카드를 수정/삭제할 수 있는지 여부
     */
    private boolean canModify;

    /**
     * 현재 사용자가 이 카드를 삭제할 수 있는지 여부
     */
    private boolean canDelete;
}