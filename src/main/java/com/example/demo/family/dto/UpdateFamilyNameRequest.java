package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 가족 이름 변경 요청 DTO
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFamilyNameRequest {

    /**
     * 새로운 가족 이름 (최대 10자)
     */
    private String name;
} 