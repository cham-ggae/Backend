package com.example.demo.family.service;

import com.example.demo.family.dao.FamilyDao;
import com.example.demo.family.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.plant.dao.PlantDao;
import com.example.demo.plant.service.PlantService;
import com.example.demo.plant.dto.PlantStatusResponseDto;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 가족 스페이스 관련 비즈니스 로직 처리 서비스
 * JWT 기반 인증 및 보안 강화 버전
 *
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class FamilyService {
    @Autowired
    private FamilyDao familyDao;

    // Plant 관련 의존성 추가
    @Autowired
    private PlantDao plantDao;

    @Autowired
    private PlantService plantService;

    // ========================================
    // 1. 가족 스페이스 생성
    // ========================================

    /**
     * 새로운 가족 스페이스 생성
     * 이미 가족에 속한 사용자는 생성 불가
     *
     * @param uid 생성자 사용자 ID (JWT에서 추출)
     * @param request 가족 생성 요청 정보
     * @return 생성 결과
     */
    @Transactional
    public CreateFamilyResponse createFamilySpace(Long uid, CreateFamilyRequest request) {
        try {
            // 1. 사용자 존재 여부 검증
            if (!familyDao.isUserExists(uid)) {
                throw new FamilyServiceException("존재하지 않는 사용자입니다. (사용자 ID: " + uid + ")");
            }

            // 2. 입력값 검증
            validateCreateFamilyRequest(request);

            // 3. 이미 가족에 속해있는지 검증 (강화된 로직)
            Long currentFamilyId = familyDao.getUserCurrentFamilyId(uid);
            if (currentFamilyId != null) {
                // 현재 가족 이름 조회
                FamilySpace currentFamily = familyDao.getFamilySpaceById(currentFamilyId);
                String familyName = currentFamily != null ? currentFamily.getName() : "알 수 없는 가족";

                throw new AlreadyInFamilyException(
                        "이미 가족 '" + familyName + "'에 속해있습니다. " +
                                "새로운 가족을 만들려면 먼저 현재 가족에서 나가주세요."
                );
            }

            // 4. 고유한 초대 코드 생성
            String inviteCode = generateUniqueInviteCode();

            // 5. 새로운 가족 스페이스 생성
            FamilySpace newFamily = new FamilySpace();
            newFamily.setName(request.getName().trim());
            newFamily.setInviteCode(inviteCode);
            newFamily.setCombiType(request.getCombiType().trim());
            newFamily.setNutrial(0); // 초기 영양제 0개

            familyDao.createFamilySpace(newFamily);

            // 6. 생성자를 가족 구성원으로 추가
            familyDao.updateUserFamilyId(uid, newFamily.getFid());

            return CreateFamilyResponse.success(newFamily, "가족 스페이스가 성공적으로 생성되었습니다.");

        } catch (AlreadyInFamilyException e) {
            return CreateFamilyResponse.failure(e.getMessage());
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

        if (request.getName().trim().length() > 10) {
            throw new FamilyServiceException("가족 이름은 10자를 초과할 수 없습니다.");
        }

        if (request.getCombiType() == null || request.getCombiType().trim().isEmpty()) {
            throw new FamilyServiceException("결합 상품 타입은 필수입니다.");
        }

        // 결합 상품 타입 유효성 검증
        String[] validCombiTypes = {
                "투게더 결합", "참쉬운 가족 결합"
        };

        boolean isValidCombiType = false;
        for (String validType : validCombiTypes) {
            if (validType.equals(request.getCombiType().trim())) {
                isValidCombiType = true;
                break;
            }
        }

        if (!isValidCombiType) {
            throw new FamilyServiceException("유효하지 않은 결합 상품 타입입니다.");
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

    // ========================================
    // 2. 가족 참여 (초대 코드로 조인)
    // ========================================

    /**
     * 초대 코드를 통한 가족 참여
     * 이미 가족에 속한 사용자는 참여 불가
     *
     * @param uid 참여할 사용자 ID (JWT에서 추출)
     * @param inviteCode 초대 코드
     * @return 참여 결과
     */
    @Transactional
    public CreateFamilyResponse joinFamilySpace(Long uid, String inviteCode) {
        try {
            // 1. 사용자 존재 여부 검증
            if (!familyDao.isUserExists(uid)) {
                throw new FamilyServiceException("존재하지 않는 사용자입니다. (사용자 ID: " + uid + ")");
            }

            // 2. 초대 코드 유효성 검증
            if (inviteCode == null || inviteCode.trim().isEmpty()) {
                throw new FamilyServiceException("초대 코드를 입력해주세요.");
            }

            FamilySpace targetFamily = familyDao.getFamilySpaceByInviteCode(inviteCode.trim());
            if (targetFamily == null) {
                throw new FamilyServiceException("유효하지 않은 초대 코드입니다.");
            }

            // 3. 가족 구성원 수 제한 체크 (최대 5명)
            int currentMemberCount = familyDao.getFamilyMemberCount(targetFamily.getFid());
            if (currentMemberCount >= 5) {
                throw new FamilyServiceException(
                        "가족 구성원은 최대 5명까지 가능합니다. (현재: " + currentMemberCount + "/5명)"
                );
            }

            // 4. 현재 다른 가족에 속해있는지 확인
            Long currentFamilyId = familyDao.getUserCurrentFamilyId(uid);
            if (currentFamilyId != null) {
                if (currentFamilyId.equals(targetFamily.getFid())) {
                    throw new FamilyServiceException("이미 해당 가족의 구성원입니다.");
                }

                // 다른 가족에 속해있다면 명시적으로 알려줌
                FamilySpace currentFamily = familyDao.getFamilySpaceById(currentFamilyId);
                String currentFamilyName = currentFamily != null ? currentFamily.getName() : "알 수 없는 가족";

                throw new AlreadyInFamilyException(
                        "이미 가족 '" + currentFamilyName + "'에 속해있습니다. " +
                                "새로운 가족에 참여하려면 먼저 현재 가족에서 나가주세요."
                );
            }

            // 5. 새로운 가족에 참여
            familyDao.updateUserFamilyId(uid, targetFamily.getFid());

            return CreateFamilyResponse.success(targetFamily,
                    "가족 '" + targetFamily.getName() + "'에 성공적으로 참여했습니다.");

        } catch (AlreadyInFamilyException e) {
            return CreateFamilyResponse.failure(e.getMessage());
        } catch (FamilyServiceException e) {
            return CreateFamilyResponse.failure(e.getMessage());
        } catch (Exception e) {
            return CreateFamilyResponse.failure("가족 참여 중 오류가 발생했습니다.");
        }
    }


    /**
     * 가족 기본 정보만 조회 (내부 사용)
     */
    public FamilySpace getFamilyBasicInfo(Long fid) {
        return familyDao.getFamilySpaceById(fid);
    }

    /**
     * 사용자가 해당 가족의 구성원인지 검증 + 사용자 존재 여부 검증
     */
    private void validateFamilyMember(Long uid, Long fid) {
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
        String[] mockUsages = {"45GB", "23GB", "67GB", "12GB", "89GB", "34GB", "78GB"};
        Random random = new Random();

        for (int i = 0; i < members.size(); i++) {
            int usageIndex = random.nextInt(mockUsages.length);
            members.get(i).setDataUsage(mockUsages[usageIndex]);
        }
    }

    /**
     * 가족 할인 정보 계산 (age 문자열 처리 수정)
     */
    private DiscountInfo calculateFamilyDiscount(FamilySpace family, List<FamilyMember> members) {
        int memberCount = members.size();
        int baseDiscount = memberCount * 14000; // 기본 할인: 인당 14,000원



        int totalDiscount = baseDiscount;

        String description = String.format(
                "%s 이용 시 한달에 최대 %,d원 아낄 수 있어요!",
                family.getCombiType(),
                totalDiscount
        );

        return new DiscountInfo(totalDiscount, description, memberCount);
    }

    // ========================================
    // 4. 초대 코드 관련
    // ========================================

    /**
     * 초대 코드 유효성 검증
     * 누구나 호출 가능 (가족 정보 미리보기용)
     *
     * @param inviteCode 검증할 초대 코드
     * @return 검증 결과
     */
    public InviteCodeValidationResponse validateInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            return InviteCodeValidationResponse.failure("초대 코드를 입력해주세요.");
        }

        FamilySpace family = familyDao.getFamilySpaceByInviteCode(inviteCode.trim());
        if (family == null) {
            return InviteCodeValidationResponse.failure("유효하지 않은 초대 코드입니다.");
        }

        int memberCount = familyDao.getFamilyMemberCount(family.getFid());
        if (memberCount >= 5) {
            return InviteCodeValidationResponse.failure(
                    "가족 구성원이 가득 찼습니다. (현재: " + memberCount + "/5명)"
            );
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
     * 해당 가족의 구성원만 실행 가능
     *
     * @param fid 가족 스페이스 ID
     * @param uid 요청자 사용자 ID (JWT에서 추출)
     * @return 새로운 초대 코드
     */
    @Transactional
    public String generateNewInviteCode(Long fid, Long uid) {
        // 권한 체크
        validateFamilyMember(uid, fid);

        // 새로운 초대 코드 생성
        String newInviteCode = generateUniqueInviteCode();

        // DB 업데이트
        int updated = familyDao.updateInviteCode(fid, newInviteCode);
        if (updated == 0) {
            throw new FamilyServiceException("초대 코드 업데이트에 실패했습니다.");
        }

        return newInviteCode;
    }

    // ========================================
    // 5. 가족 탈퇴
    // ========================================

    /**
     * 가족에서 나가기 (외래키 제약조건 고려)
     * 마지막 구성원인 경우 관련 데이터를 모두 삭제한 후 가족 스페이스 삭제
     *
     * @param uid 탈퇴할 사용자 ID (JWT에서 추출)
     * @param fid 가족 스페이스 ID
     */
    @Transactional
    public void leaveFamilySpace(Long uid, Long fid) {
        // 권한 체크
        validateFamilyMember(uid, fid);

        // 가족에서 제거
        int removed = familyDao.removeUserFromFamily(uid, fid);
        if (removed == 0) {
            throw new FamilyServiceException("가족 탈퇴 처리에 실패했습니다.");
        }

        // 가족에 구성원이 없다면 관련 데이터를 모두 삭제한 후 가족 스페이스 삭제
        int remainingMembers = familyDao.getFamilyMemberCount(fid);
        if (remainingMembers == 0) {
            try {
                // 1. 가족 관련 모든 데이터 삭제 (외래키 순서대로)
                deleteAllFamilyRelatedData(fid);

                // 2. 마지막으로 가족 스페이스 삭제
                familyDao.deleteFamilySpace(fid);

                log.info("가족 스페이스 완전 삭제 완료: fid={}", fid);

            } catch (Exception e) {
                log.error("가족 스페이스 삭제 중 오류 발생: fid={}, error={}", fid, e.getMessage());
                // 가족 스페이스 삭제 실패 시에도 사용자 탈퇴는 성공으로 처리
                // (데이터 정합성을 위해 별도 배치 작업으로 정리 가능)
            }
        }
    }

    /**
     * 가족 관련 모든 데이터 삭제 (외래키 순서 고려)
     */
    private void deleteAllFamilyRelatedData(Long fid) {
        try {
            // 1. Point_activities 삭제 (Plants를 참조하므로 먼저 삭제)
            familyDao.deletePointActivitiesByFid(fid);

            // 2. reward_log 삭제 (Plants를 참조하므로 먼저 삭제)
            familyDao.deleteRewardLogByFid(fid);

            // 3. Plants 삭제 (Family_space를 참조)
            familyDao.deletePlantsByFid(fid);

            // 4. Family_cards_comment 삭제 (Family_cards를 참조)
            familyDao.deleteFamilyCardCommentsByFid(fid);

            // 5. Family_cards 삭제 (Family_space와 연관)
            familyDao.deleteFamilyCardsByFid(fid);

            log.debug("가족 관련 데이터 삭제 완료: fid={}", fid);

        } catch (Exception e) {
            log.error("가족 관련 데이터 삭제 중 오류: fid={}, error={}", fid, e.getMessage());
            throw new FamilyServiceException("가족 데이터 정리 중 오류가 발생했습니다.");
        }
    }

    // ========================================
    // 6. 사용자 가족 정보 조회
    // ========================================

    /**
     * 사용자가 현재 속한 가족 ID 조회
     *
     * @param uid 사용자 ID (JWT에서 추출)
     * @return 가족 ID (속해있지 않으면 null)
     */
    public Long getUserCurrentFamilyId(Long uid) {
        // 사용자 존재 여부 확인
        if (!familyDao.isUserExists(uid)) {
            throw new FamilyServiceException("존재하지 않는 사용자입니다. (사용자 ID: " + uid + ")");
        }

        return familyDao.getUserCurrentFamilyId(uid);
    }

    // ========================================
    // 7. 가족 관리 기능 (추가)
    // ========================================

    /**
     * 가족 이름 변경
     * 해당 가족의 구성원만 실행 가능
     *
     * @param fid 가족 스페이스 ID
     * @param uid 요청자 사용자 ID (JWT에서 추출)
     * @param newName 새로운 가족 이름
     */
    @Transactional
    public void updateFamilyName(Long fid, Long uid, String newName) {
        // 권한 체크
        validateFamilyMember(uid, fid);

        // 이름 유효성 검증
        if (newName == null || newName.trim().isEmpty()) {
            throw new FamilyServiceException("가족 이름은 필수입니다.");
        }

        if (newName.trim().length() > 10) {
            throw new FamilyServiceException("가족 이름은 10자를 초과할 수 없습니다.");
        }

        // 이름 업데이트
        int updated = familyDao.updateFamilyName(fid, newName.trim());
        if (updated == 0) {
            throw new FamilyServiceException("가족 이름 변경에 실패했습니다.");
        }
    }

    // ========================================
    // 8. 예외 클래스 정의
    // ========================================

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

    /**
     * 이미 가족에 속해있을 때 발생하는 예외
     */
    public static class AlreadyInFamilyException extends RuntimeException {
        public AlreadyInFamilyException(String message) {
            super(message);
        }
    }
    /**
     * 가족 스페이스 대시보드 정보 조회 (간소화된 식물 정보 포함)
     *
     * @param fid 가족 스페이스 ID
     * @param uid 요청자 사용자 ID (JWT에서 추출)
     * @return 대시보드 정보 (간소화된 식물 정보 포함)
     */
    public FamilyDashboardResponse getFamilyDashboardWithPlant(Long fid, Long uid) {
        // 1. 권한 체크
        validateFamilyMember(uid, fid);

        // 2. 가족 기본 정보 조회
        FamilySpace family = familyDao.getFamilySpaceById(fid);
        if (family == null) {
            throw new FamilyServiceException("존재하지 않는 가족 스페이스입니다.");
        }

        // 3. 가족 구성원 목록 조회
        List<FamilyMember> members = familyDao.getFamilyMembers(fid);
        setMockDataUsage(members); // 임시 데이터 사용량 설정

        // 4. 할인 정보 계산
        DiscountInfo discount = calculateFamilyDiscount(family, members);

        // 5. 간소화된 식물 정보 생성
        PlantInfo plantInfo = createSimplePlantInfo(fid);

        // 6. 응답 객체 생성 (4개 매개변수)
        return new FamilyDashboardResponse(family, members, discount, plantInfo);
    }

    /**
     * 간소화된 식물 정보 생성 (기존 검증된 메서드 활용)
     */
    private PlantInfo createSimplePlantInfo(Long fid) {
        try {
            // 1. 구성원 수 확인 (기존 검증된 메서드 사용)
            int memberCount = familyDao.getFamilyMemberCount(fid);
            log.debug("Family {} member count: {}", fid, memberCount);

            if (memberCount < 2) {
                log.debug("Cannot create plant - insufficient members: {}", memberCount);
                return PlantInfo.noPlant(false, "가족 구성원이 2명 이상이어야 합니다.");
            }

            // 2. 미완료 식물 존재 여부 확인 (기존 검증된 메서드 사용)
            boolean hasUncompletedPlant = false;
            try {
                // 기존 PlantDao의 hasUncompletedPlant 메서드 사용
                hasUncompletedPlant = plantDao.hasUncompletedPlant(fid);
                log.debug("Family {} has uncompleted plant: {}", fid, hasUncompletedPlant);
            } catch (Exception e) {
                log.debug("No uncompleted plant found (normal): {}", e.getMessage());
                hasUncompletedPlant = false;
            }

            if (hasUncompletedPlant) {
                // 3. 기존 식물이 있는 경우 - 기존 PlantService 사용
                try {
                    PlantStatusResponseDto plantStatus = plantService.getLatestPlant(fid);
                    log.debug("Found existing plant: level={}, type={}, completed={}",
                            plantStatus.getLevel(), plantStatus.getPlantType(), plantStatus.isCompleted());

                    return PlantInfo.hasPlant(
                            plantStatus.getLevel(),
                            plantStatus.getPlantType(),
                            plantStatus.isCompleted() // 완료된 경우만 새로 생성 가능
                    );
                } catch (Exception e) {
                    log.warn("Failed to get plant details, using default: {}", e.getMessage());
                    return PlantInfo.hasPlant(1, "flower", false);
                }
            }

            // 4. 식물이 없고 조건을 만족하는 경우
            log.debug("Family {} can create new plant", fid);
            return PlantInfo.canCreateNew();

        } catch (Exception e) {
            log.error("Error creating plant info for family {}: {}", fid, e.getMessage(), e);
            // 더 구체적인 에러 메시지 제공
            return PlantInfo.noPlant(false, "식물 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
        }
    }

}