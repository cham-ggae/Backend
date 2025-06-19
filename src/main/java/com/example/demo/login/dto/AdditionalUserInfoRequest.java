package com.example.demo.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 사용자 추가 정보 수집 요청 DTO
 */
@Data
@Schema(description = "사용자 추가 정보 요청")
public class AdditionalUserInfoRequest {

    @Schema(description = "성별", example = "male", allowableValues = {"male", "female"})
    @NotBlank(message = "성별은 필수 입력 항목입니다.")
    @Pattern(regexp = "^(male|female)$", message = "성별은 'male' 또는 'female'만 입력 가능합니다.")
    private String gender;

    @Schema(description = "나이대", example = "20~29",
            allowableValues = {"10~19", "20~29", "30~39", "40~49", "50~59", "60~69", "70~79", "80~89", "90~99"})
    @NotBlank(message = "나이대는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(10~19|20~29|30~39|40~49|50~59|60~69|70~79|80~89|90~99)$",
            message = "올바른 나이대 형식이 아닙니다. (예: 20~29)")
    private String age;
}