package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 가족 스페이스 대시보드 응답 DTO
 * 메인 대시보드 페이지에서 사용하는 모든 정보를 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDashboardResponse {

    /**
     * 가족 스페이스 기본 정보
     */
    private FamilySpace family;

    /**
     * 가족 구성원 목록 (요금제 정보 포함)
     */
    private List<FamilyMember> members;

    /**
     * 할인 정보
     */
    private DiscountInfo discount;

    /**
     * 편의 메서드: 총 구성원 수
     */
    public int getTotalMembers() {
        return members != null ? members.size() : 0;
    }


    /**
     * 편의 메서드: 요금제 가입 구성원 수
     */
    public long getMembersWithPlan() {
        return members != null ? members.stream().filter(FamilyMember::hasPlan).count() : 0;
    }
}
