package com.example.demo.familycard.dao;

import com.example.demo.familycard.dto.FamilyCard;
import com.example.demo.familycard.dto.FamilyCardResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 가족 메시지 카드 관련 데이터베이스 접근 인터페이스
 * FamilyCard.xml MyBatis 매퍼와 연동
 */
@Mapper
public interface FamilyCardDao {

    // ========================================
    // 1. 조회 관련 메서드
    // ========================================

    /**
     * 특정 가족의 모든 메시지 카드 조회 (최신순)
     * 작성자 정보 포함하여 조회
     *
     * @param fid 가족 스페이스 ID
     * @return 메시지 카드 목록 (작성자 정보 포함)
     */
    List<FamilyCardResponse> getFamilyCardsByFamilyId(@Param("fid") Long fid);

    /**
     * 특정 메시지 카드 상세 조회
     * 작성자 정보 포함
     *
     * @param fcid 메시지 카드 ID
     * @return 메시지 카드 정보 (작성자 정보 포함), 없으면 null
     */
    FamilyCardResponse getFamilyCardById(@Param("fcid") Long fcid);

    /**
     * 메시지 카드의 기본 정보만 조회 (권한 체크용)
     *
     * @param fcid 메시지 카드 ID
     * @return 메시지 카드 기본 정보, 없으면 null
     */
    FamilyCard getFamilyCardBasicInfo(@Param("fcid") Long fcid);

    /**
     * 특정 가족의 메시지 카드 총 개수 조회
     *
     * @param fid 가족 스페이스 ID
     * @return 메시지 카드 총 개수
     */
    int getFamilyCardCount(@Param("fid") Long fid);

    // ========================================
    // 2. 생성/수정/삭제 관련 메서드
    // ========================================

    /**
     * 새로운 메시지 카드 생성
     * 생성 후 자동 생성된 fcid가 FamilyCard 객체에 설정됨
     *
     * @param familyCard 생성할 메시지 카드 정보
     * @return 영향받은 행 수 (성공 시 1)
     */
    int createFamilyCard(FamilyCard familyCard);

    /**
     * 메시지 카드 내용 수정
     * 이미지 타입과 메시지 내용 업데이트
     *
     * @param fcid 메시지 카드 ID
     * @param image 새로운 이미지 타입
     * @param content 새로운 메시지 내용
     * @return 영향받은 행 수 (성공 시 1)
     */
    int updateFamilyCard(@Param("fcid") Long fcid,
                         @Param("image") String image,
                         @Param("content") String content);

    /**
     * 메시지 카드 삭제
     *
     * @param fcid 삭제할 메시지 카드 ID
     * @return 영향받은 행 수 (성공 시 1)
     */
    int deleteFamilyCard(@Param("fcid") Long fcid);

    // ========================================
    // 3. 권한 및 검증 관련 메서드
    // ========================================

    /**
     * 메시지 카드 존재 여부 확인
     *
     * @param fcid 메시지 카드 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean isFamilyCardExists(@Param("fcid") Long fcid);

    /**
     * 사용자가 특정 메시지 카드의 작성자인지 확인
     * 삭제/수정 권한 체크에 사용
     *
     * @param fcid 메시지 카드 ID
     * @param uid 사용자 ID
     * @return 작성자이면 true, 아니면 false
     */
    boolean isCardOwner(@Param("fcid") Long fcid, @Param("uid") Long uid);

    /**
     * 메시지 카드가 특정 가족에 속하는지 확인
     * 접근 권한 체크에 사용
     *
     * @param fcid 메시지 카드 ID
     * @param fid 가족 스페이스 ID
     * @return 해당 가족의 카드이면 true, 아니면 false
     */
    boolean isCardBelongsToFamily(@Param("fcid") Long fcid, @Param("fid") Long fid);

    // ========================================
    // 4. 통계 및 기타 정보 조회
    // ========================================

    /**
     * 사용자별 메시지 카드 작성 개수 조회
     * 가족 내 활동 통계용
     *
     * @param fid 가족 스페이스 ID
     * @return Map 형태의 사용자별 카드 개수 정보
     */
    List<java.util.Map<String, Object>> getCardCountByMember(@Param("fid") Long fid);

    /**
     * 최근 N개의 메시지 카드 조회
     * 대시보드나 미리보기용
     *
     * @param fid 가족 스페이스 ID
     * @param limit 조회할 개수
     * @return 최근 메시지 카드 목록
     */
    List<FamilyCardResponse> getRecentFamilyCards(@Param("fid") Long fid, @Param("limit") int limit);
}