package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 가족 이름 변경 응답 DTO
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFamilyNameResponse {

    /**
     * 성공 여부
     */
    private boolean success;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 변경된 가족 정보 (성공 시에만 포함)
     */
    private FamilySpace family;

    /**
     * 성공 응답 생성
     */
    public static UpdateFamilyNameResponse success(FamilySpace family, String message) {
        return new UpdateFamilyNameResponse(true, message, family);
    }

    /**
     * 실패 응답 생성
     */
    public static UpdateFamilyNameResponse failure(String message) {
        return new UpdateFamilyNameResponse(false, message, null);
    }
} 