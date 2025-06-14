package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 초대 코드 검증 응답 DTO
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteCodeValidationResponse {

    /**
     * 초대 코드 유효성 여부
     */
    private boolean valid;

    /**
     * 검증 실패 시 오류 메시지
     */
    private String error;

    /**
     * 유효한 경우 가족 기본 정보
     */
    private FamilyInfo familyInfo;

    /**
     * 성공 응답 생성
     *
     * @param familyInfo 가족 기본 정보
     * @return 성공 응답 객체
     */
    public static InviteCodeValidationResponse success(FamilyInfo familyInfo) {
        return new InviteCodeValidationResponse(true, null, familyInfo);
    }

    /**
     * 실패 응답 생성
     *
     * @param error 오류 메시지
     * @return 실패 응답 객체
     */
    public static InviteCodeValidationResponse failure(String error) {
        return new InviteCodeValidationResponse(false, error, null);
    }
}