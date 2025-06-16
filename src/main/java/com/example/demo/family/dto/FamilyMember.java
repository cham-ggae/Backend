package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 가족 구성원 정보 DTO
 * Users 테이블과 Plans 테이블을 LEFT JOIN한 결과와 매핑
 * 가족 스페이스 대시보드에서 구성원 정보 표시에 사용
 * Long 타입으로 통일
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMember {

    /**
     * 사용자 고유 ID (Primary Key)
     * Users.uid와 매핑
     */
    private Long uid;

    /**
     * 사용자 이름
     * 카카오 로그인 시 제공되는 이름 또는 사용자 입력 이름
     * 최대 15자까지 저장 가능
     */
    private String name;

    /**
     * 사용자 나이
     * 청소년 할인(19세 미만) 계산에 사용
     * NULL 가능 (선택적 입력)
     */
    private Integer age;

    /**
     * 사용자 성별
     * 요금제 추천 시 참고 데이터로 사용
     * 가능한 값: "남", "여", "others"
     * 최대 3자까지 저장 가능
     */
    private String gender;

    /**
     * 서비스 가입일시
     * 장기 고객 할인 계산 기준일
     * 자동으로 현재 시간이 설정됨
     */
    private LocalDateTime joinDate;

    // 요금제 정보 (Plans 테이블 - LEFT JOIN)
    /**
     * 현재 사용 중인 요금제 ID
     * Plans.plan_id와 매핑
     * NULL인 경우 요금제 미가입 상태
     */
    private Integer planId;

    /**
     * 요금제 이름
     * 예: "5G 시그니처", "5G 스탠다드", "5G 프리미어" 등
     * NULL인 경우 요금제 미가입 상태
     */
    private String planName;

    /**
     * 요금제 월 이용료 (원)
     * 할인 전 기본 요금
     * NULL인 경우 요금제 미가입 상태
     */
    private Integer price;

    /**
     * 요금제 혜택 설명
     * 데이터, 통화, 문자 등의 제공 내용
     * 최대 150자까지 저장 가능
     */
    private String benefit;

    // UI 표시용 확장 데이터
    /**
     * 현재 데이터 사용량 표시용
     * 예: "45GB", "23GB", "무제한" 등
     * 실제 사용량은 외부 API에서 조회하거나 하드코딩
     * UI에서 시각적 표시를 위해 사용
     */
    private String dataUsage;

    /**
     * 프로필 이미지 URL (추가)
     * 카카오 로그인에서 제공되는 프로필 이미지
     */
    private String profileImage;

    // 생성자들
    public FamilyMember(Long uid, String name, Integer age, String gender, LocalDateTime joinDate) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.joinDate = joinDate;
    }

    /**
     * 요금제 가입 여부 확인
     * @return 요금제 ID와 이름이 모두 있으면 true, 그렇지 않으면 false
     */
    public boolean hasPlan() {
        return planId != null && planName != null;
    }

    /**
     * 요금제 요약 정보 생성 (UI 표시용)
     * @return 요금제명과 월 이용료를 포함한 요약 문자열
     */
    public String getPlanSummary() {
        if (!hasPlan()) {
            return "요금제 없음";
        }
        return planName + " (월 " + String.format("%,d", price) + "원)";
    }
}