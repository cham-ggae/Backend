package com.example.demo.familycard.dao;

import com.example.demo.familycard.dto.FamilyCardComment;
import com.example.demo.familycard.dto.CommentResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 가족 메시지 카드 댓글 관련 데이터베이스 접근 인터페이스
 * FamilyCardComment.xml MyBatis 매퍼와 연동
 */
@Mapper
public interface FamilyCardCommentDao {

    // ========================================
    // 1. 조회 관련 메서드
    // ========================================

    /**
     * 특정 메시지 카드의 모든 댓글 조회 (등록순)
     * 작성자 정보 포함하여 조회
     *
     * @param fcid 메시지 카드 ID
     * @return 댓글 목록 (작성자 정보 포함)
     */
    List<CommentResponse> getCommentsByCardId(@Param("fcid") Long fcid);

    /**
     * 특정 댓글 상세 조회
     * 작성자 정보 포함
     *
     * @param commentId 댓글 ID
     * @return 댓글 정보 (작성자 정보 포함), 없으면 null
     */
    CommentResponse getCommentById(@Param("commentId") Long commentId);

    /**
     * 댓글의 기본 정보만 조회 (권한 체크용)
     *
     * @param commentId 댓글 ID
     * @return 댓글 기본 정보, 없으면 null
     */
    FamilyCardComment getCommentBasicInfo(@Param("commentId") Long commentId);

    /**
     * 특정 메시지 카드의 댓글 총 개수 조회
     *
     * @param fcid 메시지 카드 ID
     * @return 댓글 총 개수
     */
    int getCommentCount(@Param("fcid") Long fcid);

    /**
     * 특정 메시지 카드의 최근 N개 댓글 조회
     * 메시지 카드 상세보기에서 미리보기용
     *
     * @param fcid 메시지 카드 ID
     * @param limit 조회할 개수
     * @return 최근 댓글 목록
     */
    List<CommentResponse> getRecentComments(@Param("fcid") Long fcid, @Param("limit") int limit);

    // ========================================
    // 2. 생성/수정/삭제 관련 메서드
    // ========================================

    /**
     * 새로운 댓글 생성
     * 생성 후 자동 생성된 commentId가 FamilyCardComment 객체에 설정됨
     *
     * @param comment 생성할 댓글 정보
     * @return 영향받은 행 수 (성공 시 1)
     */
    int createComment(FamilyCardComment comment);

    /**
     * 댓글 내용 수정
     *
     * @param commentId 댓글 ID
     * @param content 새로운 댓글 내용
     * @return 영향받은 행 수 (성공 시 1)
     */
    int updateComment(@Param("commentId") Long commentId, @Param("content") String content);

    /**
     * 댓글 삭제
     *
     * @param commentId 삭제할 댓글 ID
     * @return 영향받은 행 수 (성공 시 1)
     */
    int deleteComment(@Param("commentId") Long commentId);

    /**
     * 특정 메시지 카드의 모든 댓글 삭제
     * 메시지 카드 삭제 시 CASCADE 용도
     *
     * @param fcid 메시지 카드 ID
     * @return 삭제된 댓글 수
     */
    int deleteCommentsByCardId(@Param("fcid") Long fcid);

    // ========================================
    // 3. 권한 및 검증 관련 메서드
    // ========================================

    /**
     * 댓글 존재 여부 확인
     *
     * @param commentId 댓글 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean isCommentExists(@Param("commentId") Long commentId);

    /**
     * 사용자가 특정 댓글의 작성자인지 확인
     * 수정/삭제 권한 체크에 사용
     *
     * @param commentId 댓글 ID
     * @param uid 사용자 ID
     * @return 작성자이면 true, 아니면 false
     */
    boolean isCommentOwner(@Param("commentId") Long commentId, @Param("uid") Long uid);

    /**
     * 댓글이 특정 메시지 카드에 속하는지 확인
     * 접근 권한 체크에 사용
     *
     * @param commentId 댓글 ID
     * @param fcid 메시지 카드 ID
     * @return 해당 카드의 댓글이면 true, 아니면 false
     */
    boolean isCommentBelongsToCard(@Param("commentId") Long commentId, @Param("fcid") Long fcid);

    /**
     * 댓글이 특정 가족에 속하는지 확인
     * 가족 구성원 권한 체크에 사용
     *
     * @param commentId 댓글 ID
     * @param fid 가족 스페이스 ID
     * @return 해당 가족의 댓글이면 true, 아니면 false
     */
    boolean isCommentBelongsToFamily(@Param("commentId") Long commentId, @Param("fid") Long fid);

    // ========================================
    // 4. 통계 및 기타 정보 조회
    // ========================================

    /**
     * 사용자별 댓글 작성 개수 조회
     * 가족 내 활동 통계용
     *
     * @param fid 가족 스페이스 ID
     * @return Map 형태의 사용자별 댓글 개수 정보
     */
    List<java.util.Map<String, Object>> getCommentCountByMember(@Param("fid") Long fid);

    /**
     * 메시지 카드별 댓글 개수 조회
     * 카드 목록에서 댓글 수 표시용
     *
     * @param fid 가족 스페이스 ID
     * @return Map 형태의 카드별 댓글 개수 정보
     */
    List<java.util.Map<String, Object>> getCommentCountByCard(@Param("fid") Long fid);

    /**
     * 특정 사용자의 모든 댓글 삭제
     * 회원 탈퇴 시 사용
     *
     * @param uid 사용자 ID
     * @return 삭제된 댓글 수
     */
    int deleteCommentsByUserId(@Param("uid") Long uid);

    /**
     * 가족 전체 댓글 수 조회
     * 통계용
     *
     * @param fid 가족 스페이스 ID
     * @return 총 댓글 수
     */
    int getTotalCommentCountByFamily(@Param("fid") Long fid);
}