package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 가족 스페이스 생성/참여 응답 DTO
 *
 */
@Data
@NoArgsConstructor
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
     * 모든 필드를 매개변수로 하는 생성자
     */
    public CreateFamilyResponse(boolean success, String message, FamilySpace family) {
        this.success = success;
        this.message = message;
        this.family = family;
    }

    /**
     * 성공 응답 생성 (기본 메시지)
     *
     * @param family 가족 스페이스 정보
     * @return 성공 응답 객체
     */
    public static CreateFamilyResponse success(FamilySpace family) {
        return new CreateFamilyResponse(true, "가족 스페이스가 성공적으로 생성되었습니다.", family);
    }

    /**
     * 성공 응답 생성 (커스텀 메시지)
     *
     * @param family 가족 스페이스 정보
     * @param message 성공 메시지
     * @return 성공 응답 객체
     */
    public static CreateFamilyResponse success(FamilySpace family, String message) {
        return new CreateFamilyResponse(true, message, family);
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