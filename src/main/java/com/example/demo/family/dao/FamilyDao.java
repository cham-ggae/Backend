package com.example.demo.family.dao;

import com.example.demo.family.dto.FamilySpace;
import com.example.demo.family.dto.FamilyMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 가족 스페이스 관련 데이터베이스 접근 인터페이스
 * Family.xml MyBatis 매퍼와 연동
 */
@Mapper
public interface FamilyDao {

    // 1. 가족 스페이스 생성 관련

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
    int updateUserFamilyId(@Param("uid") Integer uid, @Param("fid") Integer fid);

    // 2. 가족 스페이스 조회 관련

    /**
     * 가족 스페이스 ID로 기본 정보 조회
     *
     * @param fid 가족 스페이스 ID
     * @return 가족 스페이스 정보, 없으면 null
     */
    FamilySpace getFamilySpaceById(@Param("fid") Integer fid);

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
    List<FamilyMember> getFamilyMembers(@Param("fid") Integer fid);

    /**
     * 가족 구성원 수 조회
     * 할인 계산 및 UI 표시에 사용
     *
     * @param fid 가족 스페이스 ID
     * @return 구성원 수
     */
    int getFamilyMemberCount(@Param("fid") Integer fid);

    // 3. 사용자 상태 확인 관련

    /**
     * 사용자가 현재 속한 가족 ID 조회
     * 다른 가족 참여 시 기존 가족 탈퇴 처리에 사용
     *
     * @param uid 사용자 ID
     * @return 현재 가족 ID, 가족에 속하지 않으면 null
     */
    Integer getUserCurrentFamilyId(@Param("uid") Integer uid);

    /**
     * 사용자가 특정 가족의 구성원인지 확인
     * 권한 체크에 사용
     *
     * @param uid 사용자 ID
     * @param fid 가족 스페이스 ID
     * @return 구성원이면 true, 아니면 false
     */
    boolean isUserFamilyMember(@Param("uid") Integer uid, @Param("fid") Integer fid);

    /**
     * 가족에서 나가기 (fid를 NULL로 설정)
     *
     * @param uid 사용자 ID
     * @param fid 가족 스페이스 ID (안전성을 위한 이중 체크)
     * @return 영향받은 행 수 (성공 시 1)
     */
    int removeUserFromFamily(@Param("uid") Integer uid, @Param("fid") Integer fid);

    // 4. 초대 코드 관리 관련

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
    int updateInviteCode(@Param("fid") Integer fid, @Param("newInviteCode") String newInviteCode);

    // 5. 가족 스페이스 관리 관련

    /**
     * 가족 스페이스 이름 변경
     *
     * @param fid 가족 스페이스 ID
     * @param name 새로운 가족 이름
     * @return 영향받은 행 수 (성공 시 1)
     */
    int updateFamilyName(@Param("fid") Integer fid, @Param("name") String name);

    /**
     * 가족 결합 상품 타입 변경
     * 할인 혜택 변경 시 사용
     *
     * @param fid 가족 스페이스 ID
     * @param combiType 새로운 결합 상품 타입
     * @return 영향받은 행 수 (성공 시 1)
     */
    int updateFamilyCombiType(@Param("fid") Integer fid, @Param("combiType") String combiType);

    /**
     * 가족 스페이스 삭제
     * 모든 구성원이 나간 경우에만 실행
     *
     * @param fid 가족 스페이스 ID
     * @return 영향받은 행 수 (성공 시 1)
     */
    int deleteFamilySpace(@Param("fid") Integer fid);

    // 6. 통계 및 기타 정보 조회

    /**
     * 가족 기본 통계 정보 조회
     * 할인 계산 및 대시보드 표시에 사용
     *
     * @param fid 가족 스페이스 ID
     * @return Map 형태의 통계 정보
     *         - totalMembers: 총 구성원 수
     *         - combiType: 결합 상품 타입
     */
    Map<String, Object> getFamilyBasicStats(@Param("fid") Integer fid);
}
