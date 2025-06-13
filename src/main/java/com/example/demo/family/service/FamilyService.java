package com.example.demo.family.service;

import com.example.demo.family.dao.FamilyDao;
import com.example.demo.family.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 가족 스페이스 관련 비즈니스 로직 처리 서비스
 *
 */
@Service
@Transactional(readOnly = true)
public class FamilyService {

    @Autowired
    private FamilyDao familyDao;

    // ========================================
    // 1. 가족 스페이스 생성
    // ========================================

    /**
     * 새로운 가족 스페이스 생성
     *
     * @param uid 생성자 사용자 ID
     * @param request 가족 생성 요청 정보
     * @return 생성 결과
     */
    @Transactional
    public CreateFamilyResponse createFamilySpace(Integer uid, CreateFamilyRequest request) {
        try {
            // 1. 사용자 존재 여부 검증 (버그 수정)
            if (!familyDao.isUserExists(uid)) {
                throw new FamilyServiceException("존재하지 않는 사용자입니다. (사용자 ID: " + uid + ")");
            }

            // 2. 입력값 검증
            validateCreateFamilyRequest(request);

            // 3. 사용자가 이미 다른 가족에 속해있다면 탈퇴 처리
            handleExistingFamily(uid);

            // 4. 고유한 초대 코드 생성
            String inviteCode = generateUniqueInviteCode();

            // 5. 새로운 가족 스페이스 생성
            FamilySpace newFamily = new FamilySpace();
            newFamily.setName(request.getName());
            newFamily.setInviteCode(inviteCode);
            newFamily.setCombiType(request.getCombiType());
            newFamily.setNutrial(0); // 초기 영양제 0개

            familyDao.createFamilySpace(newFamily);

            // 6. 생성자를 가족 구성원으로 추가
            familyDao.updateUserFamilyId(uid, newFamily.getFid());

            return CreateFamilyResponse.success(newFamily);

        } catch (FamilyServiceException e) {
            return CreateFamilyResponse.failure(e.getMessage());
        } catch (Exception e) {
            return CreateFamilyResponse.failure("가족 스페이스 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 가족 생성 요청 검증
     */
    private void validateCreateFamilyRequest(CreateFamilyRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new FamilyServiceException("가족 이름은 필수입니다.");
        }

        if (request.getName().length() > 10) {
            throw new FamilyServiceException("가족 이름은 10자를 초과할 수 없습니다.");
        }

        if (request.getCombiType() == null || request.getCombiType().trim().isEmpty()) {
            throw new FamilyServiceException("결합 상품 타입은 필수입니다.");
        }
    }

    /**
     * 사용자의 기존 가족 처리 (이미 가족에 속해있다면 탈퇴)
     */
    private void handleExistingFamily(Integer uid) {
        Integer currentFamilyId = familyDao.getUserCurrentFamilyId(uid);
        if (currentFamilyId != null) {
            familyDao.removeUserFromFamily(uid, currentFamilyId);
        }
    }

    /**
     * 중복되지 않는 고유한 초대 코드 생성
     */
    private String generateUniqueInviteCode() {
        String code;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;

        do {
            code = generateRandomCode();
            attempts++;

            if (attempts > MAX_ATTEMPTS) {
                throw new FamilyServiceException("초대 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
            }

        } while (familyDao.isInviteCodeExists(code));

        return code;
    }

    /**
     * 랜덤 초대 코드 생성 (영문 대문자 + 숫자 조합 6자리)
     * 예: A1B2C3, X9Y8Z7, M4N5O6
     */
    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // 대문자 + 숫자
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(chars.length());
            code.append(chars.charAt(index));
        }

        return code.toString();
    }

    // 2. 가족 참여 (초대 코드로 조인)

    /**
     * 초대 코드를 통한 가족 참여
     *
     * @param uid 참여할 사용자 ID
     * @param inviteCode 초대 코드
     * @return 참여 결과
     */
    @Transactional
    public CreateFamilyResponse joinFamilySpace(Integer uid, String inviteCode) {
        try {
            // 1. 사용자 존재 여부 검증 (버그 수정)
            if (!familyDao.isUserExists(uid)) {
                throw new FamilyServiceException("존재하지 않는 사용자입니다. (사용자 ID: " + uid + ")");
            }

            // 2. 초대 코드 검증
            FamilySpace targetFamily = familyDao.getFamilySpaceByInviteCode(inviteCode);
            if (targetFamily == null) {
                throw new FamilyServiceException("유효하지 않은 초대 코드입니다.");
            }

            // 3. 가족 구성원 수 제한 체크 (최대 5명)
            int currentMemberCount = familyDao.getFamilyMemberCount(targetFamily.getFid());
            if (currentMemberCount >= 5) {
                throw new FamilyServiceException("가족 구성원은 최대 5명까지 가능합니다.");
            }

            // 4. 이미 해당 가족의 구성원인지 체크
            if (familyDao.isUserFamilyMember(uid, targetFamily.getFid())) {
                throw new FamilyServiceException("이미 해당 가족의 구성원입니다.");
            }

            // 5. 기존 가족에서 탈퇴 처리
            handleExistingFamily(uid);

            // 6. 새로운 가족에 참여
            familyDao.updateUserFamilyId(uid, targetFamily.getFid());

            return CreateFamilyResponse.success(targetFamily, "가족 스페이스에 성공적으로 참여했습니다.");

        } catch (FamilyServiceException e) {
            return CreateFamilyResponse.failure(e.getMessage());
        } catch (Exception e) {
            return CreateFamilyResponse.failure("가족 참여 중 오류가 발생했습니다.");
        }
    }

    // 3. 가족 대시보드 정보 조회

    /**
     * 가족 스페이스 대시보드 정보 조회
     *
     * @param fid 가족 스페이스 ID
     * @param uid 요청자 사용자 ID
     * @return 대시보드 정보
     */
    public FamilyDashboardResponse getFamilyDashboard(Integer fid, Integer uid) {
        // 1. 권한 체크 - 해당 가족의 구성원인지 확인
        validateFamilyMember(uid, fid);

        // 2. 가족 기본 정보 조회
        FamilySpace family = familyDao.getFamilySpaceById(fid);
        if (family == null) {
            throw new FamilyServiceException("존재하지 않는 가족 스페이스입니다.");
        }

        // 3. 가족 구성원 목록 조회
        List<FamilyMember> members = familyDao.getFamilyMembers(fid);

        // 4. 데이터 사용량 임시 설정 (실제로는 외부 API 연동)
        setMockDataUsage(members);

        // 5. 할인 정보 계산
        DiscountInfo discount = calculateFamilyDiscount(family, members);

        return new FamilyDashboardResponse(family, members, discount);
    }

    /**
     * 사용자가 해당 가족의 구성원인지 검증 + 사용자 존재 여부 검증
     */
    private void validateFamilyMember(Integer uid, Integer fid) {
        // 사용자 존재 여부 확인
        if (!familyDao.isUserExists(uid)) {
            throw new FamilyServiceException("존재하지 않는 사용자입니다. (사용자 ID: " + uid + ")");
        }

        // 가족 구성원 여부 확인
        if (!familyDao.isUserFamilyMember(uid, fid)) {
            throw new FamilyAccessDeniedException("해당 가족의 구성원이 아닙니다.");
        }
    }

    /**
     * 임시 데이터 사용량 설정 (추후 실제 API 연동으로 대체)
     */
    private void setMockDataUsage(List<FamilyMember> members) {
        String[] mockUsages = {"45GB", "23GB", "67GB", "12GB", "89GB"};
        Random random = new Random();

        for (int i = 0; i < members.size(); i++) {
            members.get(i).setDataUsage(mockUsages[i % mockUsages.length]);
        }
    }

    /**
     * 가족 할인 정보 계산
     */
    private DiscountInfo calculateFamilyDiscount(FamilySpace family, List<FamilyMember> members) {
        int memberCount = members.size();
        int baseDiscount = memberCount * 14000; // 기본 할인: 인당 14,000원

        // 청소년 할인 계산 (19세 미만)
        int youthDiscount = 0;
        for (FamilyMember member : members) {
            if (member.getAge() != null && member.getAge() < 19) {
                youthDiscount += 10000; // 청소년 추가 할인 10,000원
            }
        }

        int totalDiscount = baseDiscount + youthDiscount;

        String description = String.format(
                "%s 이용 시 한달에 최대 %,d원 아낄 수 있어요!",
                family.getCombiType(),
                totalDiscount
        );

        return new DiscountInfo(totalDiscount, description, memberCount);
    }

    // 4. 초대 코드 관련

    /**
     * 초대 코드 유효성 검증
     *
     * @param inviteCode 검증할 초대 코드
     * @return 검증 결과
     */
    public InviteCodeValidationResponse validateInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            return InviteCodeValidationResponse.failure("초대 코드를 입력해주세요.");
        }

        FamilySpace family = familyDao.getFamilySpaceByInviteCode(inviteCode);
        if (family == null) {
            return InviteCodeValidationResponse.failure("유효하지 않은 초대 코드입니다.");
        }

        int memberCount = familyDao.getFamilyMemberCount(family.getFid());
        if (memberCount >= 5) {
            return InviteCodeValidationResponse.failure("가족 구성원이 가득 찼습니다. (최대 5명)");
        }

        FamilyInfo familyInfo = new FamilyInfo(
                family.getFid(),
                family.getName(),
                memberCount,
                family.getCombiType()
        );

        return InviteCodeValidationResponse.success(familyInfo);
    }

    /**
     * 새로운 초대 코드 생성 (기존 코드 갱신)
     *
     * @param fid 가족 스페이스 ID
     * @param uid 요청자 사용자 ID
     * @return 새로운 초대 코드
     */
    @Transactional
    public String generateNewInviteCode(Integer fid, Integer uid) {
        // 권한 체크
        validateFamilyMember(uid, fid);

        // 새로운 초대 코드 생성
        String newInviteCode = generateUniqueInviteCode();

        // DB 업데이트
        familyDao.updateInviteCode(fid, newInviteCode);

        return newInviteCode;
    }

    // 5. 가족 탈퇴

    /**
     * 가족에서 나가기
     *
     * @param uid 탈퇴할 사용자 ID
     * @param fid 가족 스페이스 ID
     */
    @Transactional
    public void leaveFamilySpace(Integer uid, Integer fid) {
        // 권한 체크
        validateFamilyMember(uid, fid);

        // 가족에서 제거
        familyDao.removeUserFromFamily(uid, fid);

        // 가족에 구성원이 없다면 가족 스페이스 삭제
        int remainingMembers = familyDao.getFamilyMemberCount(fid);
        if (remainingMembers == 0) {
            familyDao.deleteFamilySpace(fid);
        }
    }

    // 7. 사용자 가족 정보 조회

    /**
     * 사용자가 현재 속한 가족 ID 조회
     *
     * @param uid 사용자 ID
     * @return 가족 ID (속해있지 않으면 null)
     */
    public Integer getUserCurrentFamilyId(Integer uid) {
        // 사용자 존재 여부 확인
        if (!familyDao.isUserExists(uid)) {
            throw new FamilyServiceException("존재하지 않는 사용자입니다. (사용자 ID: " + uid + ")");
        }

        return familyDao.getUserCurrentFamilyId(uid);
    }

    // 6. 예외 클래스 정의

    /**
     * 가족 서비스 관련 비즈니스 예외
     */
    public static class FamilyServiceException extends RuntimeException {
        public FamilyServiceException(String message) {
            super(message);
        }
    }

    /**
     * 가족 접근 권한 관련 예외
     */
    public static class FamilyAccessDeniedException extends RuntimeException {
        public FamilyAccessDeniedException(String message) {
            super(message);
        }
    }
}