package com.example.demo.family.dao;

import com.example.demo.family.dto.FamilySpace;
import com.example.demo.family.dto.FamilyMember;
import com.example.demo.family.dto.FamilyMemberSurveyInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 가족 스페이스 관련 데이터베이스 접근 인터페이스
 * Family.xml MyBatis 매퍼와 연동
 * Long 타입 기반으로 통일된 인터페이스
 *
 */
@Mapper
public interface FamilyDao {

    // ========================================
    // 1. 가족 스페이스 생성 관련
    // ========================================

    /**
     * 새로운 가족 스페이스 생성
     * 생성 후 자동 생성된 fid가 FamilySpace 객체에 설정됨
     *
     * @param familySpace 생성할 가족 스페이스 정보 (fid는 자동 생성)
     * @return 영향받은 행 수 (성공 시 1)
     */
    int createFamilySpace(FamilySpace familySpace);

    /**
     * 사용자의 가족 ID 업데이트 (가족에 참여)
     * Users 테이블의 fid 필드를 업데이트
     *
     * @param uid 사용자 ID
     * @param fid 가족 스페이스 ID
     * @return 영향받은 행 수 (성공 시 1)
     */
    int updateUserFamilyId(@Param("uid") Long uid, @Param("fid") Long fid);

    // ========================================
    // 2. 가족 스페이스 조회 관련
    // ========================================

    /**
     * 가족 스페이스 ID로 기본 정보 조회
     *
     * @param fid 가족 스페이스 ID
     * @return 가족 스페이스 정보, 없으면 null
     */
    FamilySpace getFamilySpaceById(@Param("fid") Long fid);

    /**
     * 초대 코드로 가족 스페이스 조회
     * 가족 참여 시 초대 코드 검증에 사용
     *
     * @param inviteCode 초대 코드
     * @return 가족 스페이스 정보, 없으면 null
     */
    FamilySpace getFamilySpaceByInviteCode(@Param("inviteCode") String inviteCode);

    /**
     * 가족 구성원 목록 조회 (요금제 정보 포함)
     * Users와 Plans 테이블을 LEFT JOIN하여 조회
     *
     * @param fid 가족 스페이스 ID
     * @return 가족 구성원 목록 (요금제가 없는 구성원도 포함)
     */
    List<FamilyMember> getFamilyMembers(@Param("fid") Long fid);

    /**
     * 가족 구성원 수 조회
     * 할인 계산 및 UI 표시에 사용
     *
     * @param fid 가족 스페이스 ID
     * @return 구성원 수
     */
    int getFamilyMemberCount(@Param("fid") Long fid);

    /**
     * 특정 사용자의 가족 구성원 정보 조회
     * 사용자 프로필 표시에 사용
     *
     * @param uid 사용자 ID
     * @return 사용자의 가족 구성원 정보, 없으면 null
     */
    FamilyMember getFamilyMemberByUid(@Param("uid") Long uid);

    // ========================================
    // 3. 사용자 상태 확인 관련
    // ========================================

    /**
     * 사용자 존재 여부 확인
     * 가족 생성/참여 전 사용자 유효성 검증
     *
     * @param uid 사용자 ID
     * @return 사용자가 존재하면 true, 없으면 false
     */
    boolean isUserExists(@Param("uid") Long uid);

    /**
     * 사용자가 현재 속한 가족 ID 조회
     * 다른 가족 참여 시 기존 가족 탈퇴 처리에 사용
     *
     * @param uid 사용자 ID
     * @return 현재 가족 ID, 가족에 속하지 않으면 null
     */
    Long getUserCurrentFamilyId(@Param("uid") Long uid);

    /**
     * 사용자가 특정 가족의 구성원인지 확인
     * 권한 체크에 사용
     *
     * @param uid 사용자 ID
     * @param fid 가족 스페이스 ID
     * @return 구성원이면 true, 아니면 false
     */
    boolean isUserFamilyMember(@Param("uid") Long uid, @Param("fid") Long fid);

    /**
     * 가족에서 나가기 (fid를 NULL로 설정)
     *
     * @param uid 사용자 ID
     * @param fid 가족 스페이스 ID (안전성을 위한 이중 체크)
     * @return 영향받은 행 수 (성공 시 1)
     */
    int removeUserFromFamily(@Param("uid") Long uid, @Param("fid") Long fid);

    /**
     * 사용자의 기본 정보 조회 (가족 관련 정보 포함)
     *
     * @param uid 사용자 ID
     * @return 사용자 기본 정보
     */
    Map<String, Object> getUserBasicInfo(@Param("uid") Long uid);

    // ========================================
    // 4. 초대 코드 관리 관련
    // ========================================

    /**
     * 초대 코드 중복 확인
     * 새로운 초대 코드 생성 시 중복 방지에 사용
     *
     * @param inviteCode 확인할 초대 코드
     * @return 중복이면 true, 사용 가능하면 false
     */
    boolean isInviteCodeExists(@Param("inviteCode") String inviteCode);

    /**
     * 가족 스페이스의 초대 코드 업데이트
     * 새로운 초대 코드 생성 시 사용
     *
     * @param fid 가족 스페이스 ID
     * @param newInviteCode 새로운 초대 코드
     * @return 영향받은 행 수 (성공 시 1)
     */
    int updateInviteCode(@Param("fid") Long fid, @Param("newInviteCode") String newInviteCode);

    // ========================================
    // 5. 가족 스페이스 관리 관련
    // ========================================

    /**
     * 가족 스페이스 이름 변경
     *
     * @param fid 가족 스페이스 ID
     * @param name 새로운 가족 이름
     * @return 영향받은 행 수 (성공 시 1)
     */
    int updateFamilyName(@Param("fid") Long fid, @Param("name") String name);

    /**
     * 가족 결합 상품 타입 변경
     * 할인 혜택 변경 시 사용
     *
     * @param fid 가족 스페이스 ID
     * @param combiType 새로운 결합 상품 타입
     * @return 영향받은 행 수 (성공 시 1)
     */
    int updateFamilyCombiType(@Param("fid") Long fid, @Param("combiType") String combiType);

    /**
     * 가족 스페이스 영양제 수량 업데이트
     * 새싹 키우기 게임에서 사용
     *
     * @param fid 가족 스페이스 ID
     * @param nutrial 새로운 영양제 수량
     * @return 영향받은 행 수 (성공 시 1)
     */
    int updateFamilyNutrial(@Param("fid") Long fid, @Param("nutrial") Integer nutrial);

    /**
     * 가족 스페이스 삭제
     * 모든 구성원이 나간 경우에만 실행
     *
     * @param fid 가족 스페이스 ID
     * @return 영향받은 행 수 (성공 시 1)
     */
    int deleteFamilySpace(@Param("fid") Long fid);

    /**
     * 가족 스페이스 존재 여부 확인
     *
     * @param fid 가족 스페이스 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean isFamilySpaceExists(@Param("fid") Long fid);

    // ========================================
    // 6. 통계 및 기타 정보 조회
    // ========================================

    /**
     * 가족 기본 통계 정보 조회
     * 할인 계산 및 대시보드 표시에 사용
     *
     * @param fid 가족 스페이스 ID
     * @return Map 형태의 통계 정보
     *         - totalMembers: 총 구성원 수
     *         - combiType: 결합 상품 타입
     *         - youthCount: 청소년 구성원 수 (19세 미만)
     *         - avgAge: 평균 연령
     */
    Map<String, Object> getFamilyBasicStats(@Param("fid") Long fid);

    /**
     * 가족별 요금제 사용 현황 조회
     * 대시보드에서 요금제 분포 표시에 사용
     *
     * @param fid 가족 스페이스 ID
     * @return 요금제별 사용자 수 통계
     */
    List<Map<String, Object>> getFamilyPlanStats(@Param("fid") Long fid);

    /**
     * 전체 가족 스페이스 개수 조회 (관리자용)
     *
     * @return 총 가족 스페이스 수
     */
    int getTotalFamilySpaceCount();

    /**
     * 최근 생성된 가족 스페이스 목록 조회 (관리자용)
     *
     * @param limit 조회할 개수
     * @return 최근 생성된 가족 스페이스 목록
     */
    List<FamilySpace> getRecentFamilySpaces(@Param("limit") int limit);

    // ========================================
    // 7. 배치 처리용 메서드
    // ========================================

    /**
     * 구성원이 없는 빈 가족 스페이스 조회
     * 정리 배치 작업에 사용
     *
     * @return 구성원이 없는 가족 스페이스 ID 목록
     */
    List<Long> getEmptyFamilySpaces();

    /**
     * 구성원이 없는 빈 가족 스페이스 일괄 삭제
     * 정리 배치 작업에 사용
     *
     * @return 삭제된 가족 스페이스 수
     */
    int deleteEmptyFamilySpaces();

    /**
     * 가족의 현재 식물 기본 정보 조회 (레벨, 종류만)
     * 가족 스페이스 대시보드용 간소화된 정보
     *
     * @param fid 가족 스페이스 ID
     * @return 식물 기본 정보 Map (level, plantType, isCompleted)
     */
    Map<String, Object> getCurrentPlantBasicInfo(@Param("fid") Long fid);

    /**
     * 가족의 식물 생성 가능 여부 확인
     * 구성원 수와 미완료 식물 존재 여부를 한 번에 체크
     *
     * @param fid 가족 스페이스 ID
     * @return 생성 가능 정보 Map (canCreate, memberCount, hasUncompleted, reason)
     */
    Map<String, Object> getPlantCreationStatus(@Param("fid") Long fid);

    /**
     * 특정 가족의 포인트 활동 기록 삭제
     */
    int deletePointActivitiesByFid(@Param("fid") Long fid);

    /**
     * 특정 가족의 보상 기록 삭제
     */
    int deleteRewardLogByFid(@Param("fid") Long fid);

    /**
     * 특정 가족의 식물 삭제
     */
    int deletePlantsByFid(@Param("fid") Long fid);

    /**
     * 특정 가족의 메시지 카드 댓글 삭제
     */
    int deleteFamilyCardCommentsByFid(@Param("fid") Long fid);

    /**
     * 특정 가족의 메시지 카드 삭제
     */
    int deleteFamilyCardsByFid(@Param("fid") Long fid);

    /**
     * 가족 구성원들의 설문 정보 조회 (요금제 추천용)
     * Users, Bugs, Plans 테이블을 JOIN하여 조회
     *
     * @param fid 가족 스페이스 ID
     * @return 가족 구성원들의 설문 정보 목록
     */
    List<FamilyMemberSurveyInfo> getFamilyMembersSurveyInfo(@Param("fid") Long fid);

}