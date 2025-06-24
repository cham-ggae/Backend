package com.example.demo.familycard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 가족 메시지 카드 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFamilyCardRequest {

    /**
     * 이미지 타입 코드
     * 가능한 값: "heart", "flower", "star"
     */
    @NotBlank(message = "이미지 타입은 필수입니다.")
    private String imageType;

    /**
     * 메시지 내용 (최대 20자)
     */
    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(max = 20, message = "메시지는 20자 이내로 작성해주세요.")
    private String content;
}