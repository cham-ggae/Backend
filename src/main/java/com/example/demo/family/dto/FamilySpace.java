package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 가족 스페이스 기본 정보 DTO
 * Family_space 테이블과 매핑되는 엔티티
 * Long 타입으로 통일
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilySpace {

    /**
     * 가족 스페이스 고유 ID (Primary Key)
     * Family_space.fid와 매핑
     */
    private Long fid;

    /**
     * 가족 스페이스 이름
     * 사용자가 지정하는 가족명 (예: "우리가족", "김씨네 가족")
     * 최대 10자까지 저장 가능
     */
    private String name;

    /**
     * 가족 초대 코드
     * 가족 구성원 초대 시 사용하는 고유 코드
     * 영문 대문자 + 숫자 조합으로 생성 (예: "A1B2C3")
     * 고정 6자리
     */
    private String inviteCode;

    /**
     * 가족 결합 상품 타입
     * 할인 계산에 사용되는 결합 상품 종류
     * 가능한 값: "투게더 결합", "참쉬운 가족 결합", "가족 무한 사랑", "참 쉬운 케이블 가족 결합"
     */
    private String combiType;

    /**
     * 새싹 키우기 영양제 수량
     * 가족 활동을 통해 획득하는 영양제 개수
     * 식물 성장 시스템에서 사용
     */
    private Integer nutrial;

    /**
     * 가족 스페이스 생성일시
     * 자동으로 현재 시간이 설정됨 (NOW())
     */
    private LocalDateTime createdAt;

    /**
     * 편의 메서드: 가족 생성 후 경과 일수 계산
     */
    public long getDaysAfterCreation() {
        if (createdAt == null) return 0;
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
    }
}