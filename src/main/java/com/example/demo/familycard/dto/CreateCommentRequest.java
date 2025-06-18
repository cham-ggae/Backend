// CreateCommentRequest.java
package com.example.demo.familycard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 댓글 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    /**
     * 댓글 내용 (최대 100자)
     */
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 100, message = "댓글은 100자 이내로 작성해주세요.")
    private String content;
}