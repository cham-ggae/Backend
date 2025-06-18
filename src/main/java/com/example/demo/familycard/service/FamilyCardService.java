package com.example.demo.familycard.service;

import com.example.demo.familycard.dao.FamilyCardDao;
import com.example.demo.familycard.dto.*;
import com.example.demo.familycard.enums.MessageCardImageType;
import com.example.demo.family.dao.FamilyDao;
import com.example.demo.login.service.AuthenticationService;
import com.example.demo.login.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.threeten.bp.jdk8.Jdk8Methods.safeToInt;

/**
 * 가족 메시지 카드 관련 비즈니스 로직 처리 서비스
 */
@Service
@Transactional(readOnly = true)
public class FamilyCardService {

    @Autowired
    private FamilyCardDao familyCardDao;

    @Autowired
    private FamilyDao familyDao;

    @Autowired
    private AuthenticationService authenticationService;

    // ========================================
    // 1. 메시지 카드 조회 관련
    // ========================================

    /**
     * 현재 사용자 가족의 모든 메시지 카드 조회
     *
     * @return 메시지 카드 목록 응답
     */
    public FamilyCardListResponse getFamilyCards() {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 사용자의 가족 ID 조회
        Long fid = getCurrentUserFamilyId(currentUser.getUid());

        // 3. 메시지 카드 목록 조회
        List<FamilyCardResponse> cards = familyCardDao.getFamilyCardsByFamilyId(fid);

        // 4. 각 카드에 대한 권한 정보 설정
        cards.forEach(card -> setCardPermissions(card, currentUser.getUid()));

        // 5. 이미지 타입 설명 추가
        cards.forEach(this::setImageDescription);

        // 6. 가족 정보 조회
        var familySpace = familyDao.getFamilySpaceById((long) fid.intValue());
        String familyName = familySpace != null ? familySpace.getName() : "Unknown Family";

        // 7. 응답 생성
        return new FamilyCardListResponse(
                cards,
                cards.size(),
                fid,
                familyName
        );
    }

    /**
     * 특정 메시지 카드 상세 조회
     *
     * @param fcid 메시지 카드 ID
     * @return 메시지 카드 상세 정보
     */
    public FamilyCardResponse getFamilyCard(Long fcid) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 사용자의 가족 ID 조회
        Long fid = getCurrentUserFamilyId(currentUser.getUid());

        // 3. 카드가 해당 가족에 속하는지 확인
        if (!familyCardDao.isCardBelongsToFamily(fcid, fid)) {
            throw new FamilyCardServiceException("해당 가족의 메시지 카드가 아닙니다.");
        }

        // 4. 메시지 카드 조회
        FamilyCardResponse card = familyCardDao.getFamilyCardById(fcid);
        if (card == null) {
            throw new FamilyCardServiceException("존재하지 않는 메시지 카드입니다.");
        }

        // 5. 권한 정보 설정
        setCardPermissions(card, currentUser.getUid());

        // 6. 이미지 타입 설명 추가
        setImageDescription(card);

        return card;
    }

    // ========================================
    // 2. 메시지 카드 생성/수정/삭제
    // ========================================

    /**
     * 새로운 메시지 카드 생성
     *
     * @param request 카드 생성 요청
     * @return 생성된 카드 정보
     */
    @Transactional
    public FamilyCardResponse createFamilyCard(CreateFamilyCardRequest request) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 사용자의 가족 ID 조회
        Long fid = getCurrentUserFamilyId(currentUser.getUid());

        // 3. 이미지 타입 유효성 확인
        validateImageType(request.getImageType());

        // 4. 새로운 카드 생성
        FamilyCard newCard = new FamilyCard();
        newCard.setUid(currentUser.getUid());
        newCard.setImage(request.getImageType());
        newCard.setContent(request.getContent());

        int result = familyCardDao.createFamilyCard(newCard);
        if (result != 1) {
            throw new FamilyCardServiceException("메시지 카드 생성에 실패했습니다.");
        }

        // 5. 생성된 카드 조회
        FamilyCardResponse createdCard = familyCardDao.getFamilyCardById(newCard.getFcid());
        if (createdCard != null) {
            setCardPermissions(createdCard, currentUser.getUid());
            setImageDescription(createdCard);
        }

        return createdCard;
    }

    /**
     * 메시지 카드 수정
     *
     * @param fcid    메시지 카드 ID
     * @param request 카드 수정 요청
     * @return 수정된 카드 정보
     */
    @Transactional
    public FamilyCardResponse updateFamilyCard(Long fcid, UpdateFamilyCardRequest request) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 사용자의 가족 ID 조회
        Long fid = getCurrentUserFamilyId(currentUser.getUid());

        // 3. 카드 존재 및 소유권 확인
        validateCardOwnership(fcid, currentUser.getUid());

        // 4. 카드가 해당 가족에 속하는지 확인
        if (!familyCardDao.isCardBelongsToFamily(fcid, fid)) {
            throw new FamilyCardServiceException("해당 가족의 메시지 카드가 아닙니다.");
        }

        // 5. 이미지 타입 유효성 확인
        validateImageType(request.getImageType());

        // 6. 카드 수정
        int result = familyCardDao.updateFamilyCard(fcid, request.getImageType(), request.getContent());
        if (result != 1) {
            throw new FamilyCardServiceException("메시지 카드 수정에 실패했습니다.");
        }

        // 7. 수정된 카드 조회
        FamilyCardResponse updatedCard = familyCardDao.getFamilyCardById(fcid);
        if (updatedCard != null) {
            setCardPermissions(updatedCard, currentUser.getUid());
            setImageDescription(updatedCard);
        }

        return updatedCard;
    }

    /**
     * 메시지 카드 삭제
     *
     * @param fcid 메시지 카드 ID
     */
    @Transactional
    public void deleteFamilyCard(Long fcid) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 사용자의 가족 ID 조회
        Long fid = getCurrentUserFamilyId(currentUser.getUid());

        // 3. 카드 존재 확인
        if (!familyCardDao.isFamilyCardExists(fcid)) {
            throw new FamilyCardServiceException("존재하지 않는 메시지 카드입니다.");
        }

        // 4. 카드가 해당 가족에 속하는지 확인
        if (!familyCardDao.isCardBelongsToFamily(fcid, fid)) {
            throw new FamilyCardServiceException("해당 가족의 메시지 카드가 아닙니다.");
        }

        // 5. 삭제 권한 확인 (작성자 본인만 가능)
        if (!familyCardDao.isCardOwner(fcid, currentUser.getUid())) {
            throw new FamilyCardAccessDeniedException("메시지 카드를 삭제할 권한이 없습니다.");
        }

        // 6. 카드 삭제
        int result = familyCardDao.deleteFamilyCard(fcid);
        if (result != 1) {
            throw new FamilyCardServiceException("메시지 카드 삭제에 실패했습니다.");
        }
    }

    // ========================================
    // 3. 유틸리티 및 검증 메서드
    // ========================================

    /**
     * 현재 사용자의 가족 ID 조회
     */
    private Long getCurrentUserFamilyId(Long uid) {
        // Long을 Integer로 안전하게 변환하여 FamilyDao 호출
        Integer familyIdInt = Math.toIntExact(familyDao.getUserCurrentFamilyId(uid));
        if (familyIdInt == null) {
            throw new FamilyCardCommentService.CommentServiceException("가족 스페이스에 가입되어 있지 않습니다.");
        }
        // Integer를 Long으로 변환하여 반환
        return familyIdInt.longValue();
    }



    /**
     * 가족 구성원 여부 검증 (더 이상 사용하지 않지만 혹시 필요시를 위해 보존)
     */
    @Deprecated
    private void validateFamilyMember(Long uid, Long fid) {
        if (!familyDao.isUserFamilyMember((long) uid.intValue(), (long) fid.intValue())) {
            throw new FamilyCardAccessDeniedException("해당 가족의 구성원이 아닙니다.");
        }
    }

    /**
     * 이미지 타입 유효성 검증
     */
    private void validateImageType(String imageType) {
        if (!MessageCardImageType.isValidCode(imageType)) {
            throw new FamilyCardServiceException("유효하지 않은 이미지 타입입니다: " + imageType);
        }
    }

    /**
     * 카드 소유권 검증
     */
    private void validateCardOwnership(Long fcid, Long uid) {
        if (!familyCardDao.isFamilyCardExists(fcid)) {
            throw new FamilyCardServiceException("존재하지 않는 메시지 카드입니다.");
        }

        if (!familyCardDao.isCardOwner(fcid, uid)) {
            throw new FamilyCardAccessDeniedException("메시지 카드를 수정할 권한이 없습니다.");
        }
    }

    /**
     * 카드 권한 정보 설정
     */
    private void setCardPermissions(FamilyCardResponse card, Long currentUserId) {
        boolean isOwner = card.getUid().equals(currentUserId);
        card.setCanModify(isOwner);
        card.setCanDelete(isOwner);
    }

    /**
     * 이미지 타입 설명 추가
     */
    private void setImageDescription(FamilyCardResponse card) {
        try {
            MessageCardImageType imageType = MessageCardImageType.fromCode(card.getImageType());
            card.setImageDescription(imageType.getDescription());
        } catch (IllegalArgumentException e) {
            card.setImageDescription("알 수 없음");
        }
    }

    // ========================================
    // 4. 통계 및 기타 정보 조회
    // ========================================

    /**
     * 가족 구성원별 메시지 카드 작성 통계 조회
     *
     * @return 구성원별 카드 작성 통계
     */
    public List<java.util.Map<String, Object>> getFamilyCardStatistics() {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 사용자의 가족 ID 조회
        Long fid = getCurrentUserFamilyId(currentUser.getUid());

        // 3. 통계 조회
        return familyCardDao.getCardCountByMember(fid);
    }

    /**
     * 최근 메시지 카드 조회 (대시보드용)
     *
     * @param limit 조회할 개수
     * @return 최근 메시지 카드 목록
     */
    public List<FamilyCardResponse> getRecentFamilyCards(int limit) {
        // 1. 현재 사용자 조회
        User currentUser = authenticationService.getCurrentUser();

        // 2. 사용자의 가족 ID 조회
        Long fid = getCurrentUserFamilyId(currentUser.getUid());

        // 3. 최근 카드 조회
        List<FamilyCardResponse> cards = familyCardDao.getRecentFamilyCards(fid, limit);

        // 4. 권한 정보 및 이미지 설명 설정
        cards.forEach(card -> {
            setCardPermissions(card, currentUser.getUid());
            setImageDescription(card);
        });

        return cards;
    }

    // ========================================
    // 5. 예외 클래스 정의
    // ========================================

    /**
     * 가족 메시지 카드 관련 비즈니스 예외
     */
    public static class FamilyCardServiceException extends RuntimeException {
        public FamilyCardServiceException(String message) {
            super(message);
        }

        public FamilyCardServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 가족 메시지 카드 접근 권한 관련 예외
     */
    public static class FamilyCardAccessDeniedException extends RuntimeException {
        public FamilyCardAccessDeniedException(String message) {
            super(message);
        }
    }
}