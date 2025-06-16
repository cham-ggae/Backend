package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMember {

    private Integer uid;
    private String name;

    // 🔧 Integer -> String으로 변경
    private String age;  // "20~29" 형태의 문자열

    private String gender;
    private LocalDateTime joinDate;

    // 요금제 정보 (Plans 테이블 - LEFT JOIN)
    private Integer planId;
    private String planName;
    private Integer price;
    private String benefit;
    private String dataUsage;
    private String profileImage;
    private String planSummary;

    // 사용자 정보만 있는 생성자
    public FamilyMember(Integer uid, String name, String age, String gender, LocalDateTime joinDate) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.joinDate = joinDate;
    }

    /**
     * 요금제 가입 여부 확인
     */
    public boolean hasPlan() {
        return planId != null && planName != null;
    }

    /**
     * 요금제 요약 정보 생성 (UI 표시용)
     */
    public String getPlanSummary() {
        if (!hasPlan()) {
            return "요금제 없음";
        }
        return planName + " (월 " + price + "원)";
    }

    /**
     * 청소년 할인 대상 여부 확인 (19세 미만)
     * age 문자열에서 숫자를 추출하여 판단
     */
    public boolean isYouthDiscountEligible() {
        if (age == null || age.trim().isEmpty()) {
            return false;
        }

        try {
            // "20~29" 형태에서 첫 번째 숫자 추출
            String[] parts = age.split("~");
            if (parts.length > 0) {
                int ageNumber = Integer.parseInt(parts[0].trim());
                return ageNumber < 19;
            }
        } catch (NumberFormatException e) {
            // 파싱 실패 시 false 반환
            return false;
        }

        return false;
    }

    /**
     * 나이 범위의 중간값 반환 (할인 계산용)
     */
    public int getAgeMiddleValue() {
        if (age == null || age.trim().isEmpty()) {
            return 25; // 기본값
        }

        try {
            if (age.contains("~")) {
                String[] parts = age.split("~");
                int start = Integer.parseInt(parts[0].trim());
                int end = Integer.parseInt(parts[1].trim());
                return (start + end) / 2;
            } else {
                return Integer.parseInt(age.trim());
            }
        } catch (NumberFormatException e) {
            return 25; // 파싱 실패 시 기본값
        }
    }
}