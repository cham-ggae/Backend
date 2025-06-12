package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 가족 스페이스 생성/참여 응답 DTO
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFamilyResponse {

    /**
     * 요청 성공 여부
     */
    private boolean success;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 생성/참여된 가족 스페이스 정보
     */
    private FamilySpace family;

    /**
     * 성공 응답 생성
     *
     * @param family 가족 스페이스 정보
     * @return 성공 응답 객체
     */
    public static CreateFamilyResponse success(FamilySpace family) {
        return new CreateFamilyResponse(true, "가족 스페이스가 성공적으로 생성되었습니다.", family);
    }

    /**
     * 실패 응답 생성
     *
     * @param message 실패 메시지
     * @return 실패 응답 객체
     */
    public static CreateFamilyResponse failure(String message) {
        return new CreateFamilyResponse(false, message, null);
    }
}