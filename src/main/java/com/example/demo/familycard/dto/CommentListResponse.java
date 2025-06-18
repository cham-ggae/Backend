package com.example.demo.familycard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import com.example.demo.familycard.dto.CommentResponse;

/**
 * 댓글 목록 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentListResponse {

    /**
     * 댓글 목록
     */
    private List<CommentResponse> comments;

    /**
     * 총 댓글 개수
     */
    private Integer totalCount;

    /**
     * 메시지 카드 ID
     */
    private Long fcid;

    /**
     * 메시지 카드 제목/내용 (참고용)
     */
    private String cardContent;
}