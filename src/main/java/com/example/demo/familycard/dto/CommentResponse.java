package com.example.demo.familycard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 가족 메시지 카드 댓글 응답 DTO
 * 클라이언트에게 전달되는 댓글 정보 + 작성자 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    /**
     * 댓글 고유 ID
     */
    private Long commentId;

    /**
     * 메시지 카드 ID
     */
    private Long fcid;

    /**
     * 댓글 작성자 사용자 ID
     */
    private Long uid;

    /**
     * 댓글 작성자 이름
     */
    private String authorName;

    /**
     * 댓글 작성자 프로필 이미지 URL
     */
    private String authorProfileImage;

    /**
     * 댓글 내용
     */
    private String content;

    /**
     * 댓글 작성일시
     */
    private LocalDateTime createdAt;

    /**
     * 현재 사용자가 이 댓글을 수정할 수 있는지 여부
     */
    private boolean canModify;

    /**
     * 현재 사용자가 이 댓글을 삭제할 수 있는지 여부
     */
    private boolean canDelete;
}