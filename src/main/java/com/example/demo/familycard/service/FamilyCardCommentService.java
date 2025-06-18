package com.example.demo.familycard.service;

import com.example.demo.familycard.dao.FamilyCardCommentDao;
import com.example.demo.familycard.dao.FamilyCardDao;
import com.example.demo.familycard.dto.*;
import com.example.demo.family.dao.FamilyDao;
import com.example.demo.login.service.AuthenticationService;
import com.example.demo.login.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 가족 메시지 카드 댓글 관련 비즈니스 로직 처리 서비스
 */
@Service
@Transactional(readOnly = true)
public class FamilyCardCommentService {

    @Autowired
    private FamilyCardCommentDao commentDao;

    @Autowired
    private FamilyCardDao familyCardDao;

    @Autowired
    private FamilyDao familyDao;

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * 특정 메시지 카드의 모든 댓글 조회
     *
     * @param fcid 메시지 카드 ID
     * @return 댓글 목록 응답
     */
    public CommentListResponse getCardComments(Long fcid) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 사용자의 가족 ID 조회 및 카드 접근 권한 확인
        validateCardAccess(fcid, currentUser.getUid());

        // 3. 댓글 목록 조회
        List<CommentResponse> comments = commentDao.getCommentsByCardId(fcid);

        // 4. 각 댓글에 대한 권한 정보 설정
        comments.forEach(comment -> setCommentPermissions(comment, currentUser.getUid(), fcid));

        // 5. 메시지 카드 정보 조회
        FamilyCardResponse card = familyCardDao.getFamilyCardById(fcid);
        String cardContent = card != null ? card.getContent() : "Unknown Card";

        return new CommentListResponse(comments, comments.size(), fcid, cardContent);
    }

    /**
     * 특정 댓글 상세 조회
     *
     * @param fcid 메시지 카드 ID
     * @param commentId 댓글 ID
     * @return 댓글 상세 정보
     */
    public CommentResponse getComment(Long fcid, Long commentId) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 카드 접근 권한 확인
        validateCardAccess(fcid, currentUser.getUid());

        // 3. 댓글이 해당 카드에 속하는지 확인
        if (!commentDao.isCommentBelongsToCard(commentId, fcid)) {
            throw new CommentServiceException("해당 메시지 카드의 댓글이 아닙니다.");
        }

        // 4. 댓글 조회
        CommentResponse comment = commentDao.getCommentById(commentId);
        if (comment == null) {
            throw new CommentServiceException("존재하지 않는 댓글입니다.");
        }

        // 5. 권한 정보 설정
        setCommentPermissions(comment, currentUser.getUid(), fcid);

        return comment;
    }

    /**
     * 새로운 댓글 작성
     *
     * @param fcid 메시지 카드 ID
     * @param request 댓글 작성 요청
     * @return 생성된 댓글 정보
     */
    @Transactional
    public CommentResponse createComment(Long fcid, CreateCommentRequest request) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 카드 접근 권한 확인
        validateCardAccess(fcid, currentUser.getUid());

        // 3. 새로운 댓글 생성
        FamilyCardComment newComment = new FamilyCardComment();
        newComment.setFcid(fcid);
        newComment.setUid(currentUser.getUid());
        newComment.setContent(request.getContent());

        int result = commentDao.createComment(newComment);
        if (result != 1) {
            throw new CommentServiceException("댓글 작성에 실패했습니다.");
        }

        // 4. 생성된 댓글 조회
        CommentResponse createdComment = commentDao.getCommentById(newComment.getCommentId());
        if (createdComment != null) {
            setCommentPermissions(createdComment, currentUser.getUid(), fcid);
        }

        return createdComment;
    }

    /**
     * 댓글 수정
     *
     * @param fcid 메시지 카드 ID
     * @param commentId 댓글 ID
     * @param request 댓글 수정 요청
     * @return 수정된 댓글 정보
     */
    @Transactional
    public CommentResponse updateComment(Long fcid, Long commentId, UpdateCommentRequest request) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 카드 접근 권한 확인
        validateCardAccess(fcid, currentUser.getUid());

        // 3. 댓글 존재 및 소유권 확인
        validateCommentOwnership(commentId, fcid, currentUser.getUid());

        // 4. 댓글 수정
        int result = commentDao.updateComment(commentId, request.getContent());
        if (result != 1) {
            throw new CommentServiceException("댓글 수정에 실패했습니다.");
        }

        // 5. 수정된 댓글 조회
        CommentResponse updatedComment = commentDao.getCommentById(commentId);
        if (updatedComment != null) {
            setCommentPermissions(updatedComment, currentUser.getUid(), fcid);
        }

        return updatedComment;
    }

    /**
     * 댓글 삭제
     *
     * @param fcid 메시지 카드 ID
     * @param commentId 댓글 ID
     */
    @Transactional
    public void deleteComment(Long fcid, Long commentId) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 카드 접근 권한 확인
        validateCardAccess(fcid, currentUser.getUid());

        // 3. 댓글 존재 확인
        if (!commentDao.isCommentExists(commentId)) {
            throw new CommentServiceException("존재하지 않는 댓글입니다.");
        }

        // 4. 댓글이 해당 카드에 속하는지 확인
        if (!commentDao.isCommentBelongsToCard(commentId, fcid)) {
            throw new CommentServiceException("해당 메시지 카드의 댓글이 아닙니다.");
        }

        // 5. 삭제 권한 확인 (댓글 작성자 또는 카드 작성자)
        validateDeletePermission(commentId, fcid, currentUser.getUid());

        // 6. 댓글 삭제
        int result = commentDao.deleteComment(commentId);
        if (result != 1) {
            throw new CommentServiceException("댓글 삭제에 실패했습니다.");
        }
    }

    /**
     * 가족 구성원별 댓글 작성 통계 조회
     *
     * @return 구성원별 댓글 작성 통계
     */
    public List<java.util.Map<String, Object>> getCommentStatistics() {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 사용자의 가족 ID 조회
        Long fid = getCurrentUserFamilyId(currentUser.getUid());

        // 3. 통계 조회
        return commentDao.getCommentCountByMember(fid);
    }

    /**
     * 메시지 카드별 댓글 수 통계 조회
     *
     * @return 카드별 댓글 수 통계
     */
    public List<java.util.Map<String, Object>> getCardCommentStatistics() {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 사용자의 가족 ID 조회
        Long fid = getCurrentUserFamilyId(currentUser.getUid());

        // 3. 통계 조회
        return commentDao.getCommentCountByCard(fid);
    }

    /**
     * 특정 카드의 최근 댓글 조회 (미리보기용)
     *
     * @param fcid 메시지 카드 ID
     * @param limit 조회할 개수
     * @return 최근 댓글 목록
     */
    public List<CommentResponse> getRecentComments(Long fcid, int limit) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 카드 접근 권한 확인
        validateCardAccess(fcid, currentUser.getUid());

        // 3. 최근 댓글 조회
        List<CommentResponse> comments = commentDao.getRecentComments(fcid, limit);

        // 4. 권한 정보 설정
        comments.forEach(comment -> setCommentPermissions(comment, currentUser.getUid(), fcid));

        return comments;
    }

    /**
     * 현재 사용자의 가족 ID 조회
     */
    private Long getCurrentUserFamilyId(Long uid) {
        // Long을 Integer로 변환하여 FamilyDao 호출 (기존 구조 유지)
        Long familyIdInt = familyDao.getUserCurrentFamilyId(uid);
        if (familyIdInt == null) {
            throw new CommentServiceException("가족 스페이스에 가입되어 있지 않습니다.");
        }
        // Integer를 Long으로 변환하여 반환
        return familyIdInt.longValue();
    }

    /**
     * 카드 접근 권한 검증
     */
    private void validateCardAccess(Long fcid, Long uid) {
        // 1. 사용자의 가족 ID 조회
        Long fid = getCurrentUserFamilyId(uid);

        // 2. 카드 존재 여부 확인
        if (!familyCardDao.isFamilyCardExists(fcid)) {
            throw new CommentServiceException("존재하지 않는 메시지 카드입니다.");
        }

        // 3. 카드가 해당 가족에 속하는지 확인
        if (!familyCardDao.isCardBelongsToFamily(fcid, fid)) {
            throw new CommentAccessDeniedException("해당 가족의 메시지 카드가 아닙니다.");
        }
    }

    /**
     * Long을 Integer로 안전하게 변환
     */
    private Integer safeToInt(Long value) {
        if (value == null) {
            return null;
        }
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new CommentServiceException("ID 값이 범위를 초과했습니다: " + value);
        }
        return value.intValue();
    }

    /**
     * 댓글 소유권 검증 (수정용)
     */
    private void validateCommentOwnership(Long commentId, Long fcid, Long uid) {
        if (!commentDao.isCommentExists(commentId)) {
            throw new CommentServiceException("존재하지 않는 댓글입니다.");
        }

        if (!commentDao.isCommentBelongsToCard(commentId, fcid)) {
            throw new CommentServiceException("해당 메시지 카드의 댓글이 아닙니다.");
        }

        if (!commentDao.isCommentOwner(commentId, uid)) {
            throw new CommentAccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }
    }

    /**
     * 댓글 삭제 권한 검증 (댓글 작성자 또는 카드 작성자)
     */
    private void validateDeletePermission(Long commentId, Long fcid, Long uid) {
        boolean isCommentOwner = commentDao.isCommentOwner(commentId, uid);
        boolean isCardOwner = familyCardDao.isCardOwner(fcid, uid);

        if (!isCommentOwner && !isCardOwner) {
            throw new CommentAccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }
    }

    /**
     * 댓글 권한 정보 설정
     */
    private void setCommentPermissions(CommentResponse comment, Long currentUserId, Long fcid) {
        boolean isCommentOwner = comment.getUid().equals(currentUserId);
        boolean isCardOwner = familyCardDao.isCardOwner(fcid, currentUserId);

        comment.setCanModify(isCommentOwner);
        comment.setCanDelete(isCommentOwner || isCardOwner);
    }

    /**
     * 메시지 카드 삭제 시 관련 댓글 일괄 삭제 (카드 서비스에서 호출용)
     *
     * @param fcid 메시지 카드 ID
     * @return 삭제된 댓글 수
     */
    @Transactional
    public int deleteCommentsByCardId(Long fcid) {
        return commentDao.deleteCommentsByCardId(fcid);
    }

    /**
     * 회원 탈퇴 시 사용자의 모든 댓글 삭제 (사용자 서비스에서 호출용)
     *
     * @param uid 사용자 ID
     * @return 삭제된 댓글 수
     */
    @Transactional
    public int deleteCommentsByUserId(Long uid) {
        return commentDao.deleteCommentsByUserId(uid);
    }

    /**
     * 특정 카드의 댓글 수 조회 (카드 목록에서 사용)
     *
     * @param fcid 메시지 카드 ID
     * @return 댓글 수
     */
    public int getCommentCount(Long fcid) {
        return commentDao.getCommentCount(fcid);
    }

    /**
     * 댓글 서비스 관련 비즈니스 예외
     */
    public static class CommentServiceException extends RuntimeException {
        public CommentServiceException(String message) {
            super(message);
        }

        public CommentServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 댓글 접근 권한 관련 예외
     */
    public static class CommentAccessDeniedException extends RuntimeException {
        public CommentAccessDeniedException(String message) {
            super(message);
        }
    }
}