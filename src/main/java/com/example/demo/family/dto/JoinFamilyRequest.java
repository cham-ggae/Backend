package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 가족 참여 요청 DTO (초대 코드로 가족 참여)
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinFamilyRequest {

    /**
     * 가족 초대 코드 (6자리 영문 대문자 + 숫자)
     * 예: "A1B2C3", "X9Y8Z7"
     */
    private String inviteCode;
}