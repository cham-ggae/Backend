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
import java.util.HashMap;
import java.util.ArrayList;

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

    // 가족 요금제 추천 서비스 의존성 추가
    @Autowired
    private FamilyPlanRecommendationService familyPlanRecommendationService;

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
     * 사용자의 추천 요금제를 실제 plan_id로 업데이트 및 두 개의 추천 요금제 정보 설정
     * 설문조사 완료 시 suggest1을 기본 plan_id로 설정하고, suggest1과 suggest2 모두 추천 목록에 추가
     */
    private void updateUserPlanFromSurvey(List<FamilyMember> members) {
        for (FamilyMember member : members) {
            if (member.getBugId() != null) {
                try {
                    // 1. 두 개의 추천 요금제 정보 조회 (suggest1, suggest2)
                    List<Map<String, Object>> recommendedPlansData = familyDao.getRecommendedPlansDetailByUser(member.getUid());
                    
                    if (recommendedPlansData != null && !recommendedPlansData.isEmpty()) {
                        // 2. 추천 요금제 목록 구성
                        List<FamilyMember.RecommendedPlan> recommendedPlans = new ArrayList<>();
                        
                        for (Map<String, Object> planData : recommendedPlansData) {
                            FamilyMember.RecommendedPlan recommendedPlan = new FamilyMember.RecommendedPlan(
                                (Integer) planData.get("rank"),
                                (Integer) planData.get("planId"),
                                (String) planData.get("planName"),
                                (Integer) planData.get("price"),
                                (Integer) planData.get("discountPrice"),
                                (String) planData.get("benefit"),
                                (String) planData.get("link")
                            );
                            recommendedPlans.add(recommendedPlan);
                        }
                        
                        member.setRecommendedPlans(recommendedPlans);
                        
                        // 3. plan_id가 없는 경우에만 suggest1을 기본 plan_id로 설정
                        if (member.getPlanId() == null && !recommendedPlansData.isEmpty()) {
                            Map<String, Object> firstPlan = recommendedPlansData.get(0); // suggest1 (rank = 1)
                            Integer suggest1PlanId = (Integer) firstPlan.get("planId");
                            
                            if (suggest1PlanId != null) {
                                // Users 테이블의 plan_id 업데이트
                                familyDao.updateUserPlanId(member.getUid(), suggest1PlanId);
                                
                                // 현재 응답 데이터에도 반영
                                member.setPlanId(suggest1PlanId);
                                member.setPlanName((String) firstPlan.get("planName"));
                                member.setPrice((Integer) firstPlan.get("price"));
                                member.setBenefit((String) firstPlan.get("benefit"));
                                
                                log.info("사용자 {}의 기본 요금제 {}가 적용되었습니다.", member.getName(), suggest1PlanId);
                            }
                        }
                        
                        log.info("사용자 {}에게 {}개의 추천 요금제가 설정되었습니다.", member.getName(), recommendedPlans.size());
                    }
                } catch (Exception e) {
                    log.warn("사용자 {}의 추천 요금제 조회 실패: {}", member.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * 실제 데이터 사용량 조회 (Mock 데이터 대신)
     * 현재는 외부 API 연동이 없으므로 사용자별 고정값 + 랜덤 요소 사용
     */
    private void setRealDataUsage(List<FamilyMember> members) {
        // 실제 통신사 API 연동 시 이 부분을 교체
        for (FamilyMember member : members) {
            if (member.getPlanId() != null) {
                // 요금제가 있는 경우: 요금제별 적정 사용량 + 개인차
                member.setDataUsage(calculateDataUsageByPlan(member.getPlanId(), member.getUid()));
            } else {
                // 요금제가 없는 경우: 기본 사용량 패턴
                member.setDataUsage(calculateDefaultDataUsage(member.getUid()));
            }
        }
    }

    /**
     * 요금제별 데이터 사용량 계산
     */
    private String calculateDataUsageByPlan(Integer planId, Long uid) {
        // 요금제별 기본 사용량 패턴
        Map<Integer, String[]> planUsagePatterns = new HashMap<>();
        planUsagePatterns.put(1, new String[]{"80GB", "95GB", "110GB", "125GB"}); // 프리미어
        planUsagePatterns.put(2, new String[]{"60GB", "75GB", "90GB", "105GB"});  // 스탠다드
        planUsagePatterns.put(3, new String[]{"35GB", "45GB", "55GB", "65GB"});   // 심플+
        planUsagePatterns.put(4, new String[]{"25GB", "35GB", "45GB", "55GB"});   // 미니
        
        String[] patterns = planUsagePatterns.getOrDefault(planId, new String[]{"40GB", "50GB", "60GB", "70GB"});
        
        // 사용자 ID 기반으로 일관된 사용량 반환 (같은 사용자는 항상 같은 사용량)
        int index = (int) (uid % patterns.length);
        return patterns[index];
    }

    /**
     * 기본 데이터 사용량 계산 (요금제 없는 경우)
     */
    private String calculateDefaultDataUsage(Long uid) {
        String[] defaultUsages = {"20GB", "30GB", "40GB", "50GB", "60GB"};
        int index = (int) (uid % defaultUsages.length);
        return defaultUsages[index];
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

    /**
     * 가족 이름 변경 (응답 포함)
     * 해당 가족의 구성원만 실행 가능
     *
     * @param fid 가족 스페이스 ID
     * @param uid 요청자 사용자 ID (JWT에서 추출)
     * @param newName 새로운 가족 이름
     * @return 변경 결과 응답
     */
    @Transactional
    public UpdateFamilyNameResponse updateFamilyNameWithResponse(Long fid, Long uid, String newName) {
        try {
            // 기존 메서드 호출하여 이름 변경
            updateFamilyName(fid, uid, newName);

            // 변경된 가족 정보 조회
            FamilySpace updatedFamily = familyDao.getFamilySpaceById(fid);
            if (updatedFamily == null) {
                throw new FamilyServiceException("가족 정보를 찾을 수 없습니다.");
            }

            return UpdateFamilyNameResponse.success(updatedFamily, "가족 이름이 성공적으로 변경되었습니다.");

        } catch (FamilyServiceException e) {
            return UpdateFamilyNameResponse.failure(e.getMessage());
        } catch (FamilyAccessDeniedException e) {
            return UpdateFamilyNameResponse.failure(e.getMessage());
        } catch (Exception e) {
            return UpdateFamilyNameResponse.failure("가족 이름 변경 중 오류가 발생했습니다.");
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
     * 가족 스페이스 대시보드 정보 조회 (간소화된 식물 정보 + 추천 정보 포함)
     *
     * @param fid 가족 스페이스 ID
     * @param uid 요청자 사용자 ID (JWT에서 추출)
     * @return 대시보드 정보 (간소화된 식물 정보 + 추천 정보 포함)
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
        
        // 3-1. 설문조사 결과를 기반으로 요금제 자동 매핑
        updateUserPlanFromSurvey(members);
        
        // 3-2. 실제 데이터 사용량 설정
        setRealDataUsage(members);

        // 4. 할인 정보 계산
        DiscountInfo discount = calculateFamilyDiscount(family, members);

        // 5. 간소화된 식물 정보 생성
        PlantInfo plantInfo = createSimplePlantInfo(fid);

        // 6. 추천 정보 요약 생성
        FamilyDashboardResponse.RecommendationSummary recommendationSummary = createRecommendationSummary(fid, uid);

        // 7. 응답 객체 생성 (추천 정보 포함)
        FamilyDashboardResponse response = new FamilyDashboardResponse(family, members, discount, plantInfo);
        response.setRecommendationSummary(recommendationSummary);
        
        return response;
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

    /**
     * 간소화된 추천 정보 요약 생성
     * 대시보드에서 표시할 핵심 추천 정보만 생성
     *
     * @param fid 가족 스페이스 ID
     * @param uid 요청자 사용자 ID
     * @return 추천 정보 요약
     */
    private FamilyDashboardResponse.RecommendationSummary createRecommendationSummary(Long fid, Long uid) {
        try {
            // 1. 전체 추천 정보 조회 (기존 서비스 활용)
            FamilyPlanRecommendationResponse fullRecommendation = 
                    familyPlanRecommendationService.recommendFamilyPlans(fid, uid);

            // 2. 추천 불가능한 경우
            if (!fullRecommendation.isSuccess() || 
                fullRecommendation.getRecommendedPlans() == null || 
                fullRecommendation.getRecommendedPlans().isEmpty()) {
                
                return FamilyDashboardResponse.RecommendationSummary.unavailable(
                    fullRecommendation.getMessage() != null ? 
                    fullRecommendation.getMessage() : "추천 정보를 불러올 수 없습니다."
                );
            }

            // 3. 최고 추천 요금제 (1순위) 추출
            FamilyPlanRecommendationResponse.RecommendedPlan topPlan = 
                    fullRecommendation.getRecommendedPlans().get(0);
            
            FamilyDashboardResponse.TopPlan simplifiedTopPlan = new FamilyDashboardResponse.TopPlan(
                    topPlan.getPlanName(),
                    topPlan.getPrice(),
                    topPlan.getDiscountPrice(),
                    generateShortReason(topPlan.getReason()) // 한 줄로 축약
            );

            // 4. 결합 상품 정보 간소화
            FamilyDashboardResponse.CombinationInfo combinationInfo = null;
            if (fullRecommendation.getCombinationRecommendation() != null) {
                FamilyPlanRecommendationResponse.CombinationRecommendation combo = 
                        fullRecommendation.getCombinationRecommendation();
                
                combinationInfo = new FamilyDashboardResponse.CombinationInfo(
                        combo.getRecommendedCombination(),
                        combo.getTotalMonthlySavings(),
                        String.format("최대 %,d원 절약!", combo.getTotalMonthlySavings())
                );
            }

            // 5. 상태 메시지 생성
            String statusMessage = generateDashboardStatusMessage(fullRecommendation);

            // 6. 가족 유형 추출
            String familyType = fullRecommendation.getRecommendationReason() != null ? 
                    fullRecommendation.getRecommendationReason().getFamilyType() : "일반 가족";

            return FamilyDashboardResponse.RecommendationSummary.available(
                    simplifiedTopPlan, 
                    combinationInfo, 
                    familyType, 
                    statusMessage
            );

        } catch (Exception e) {
            log.warn("추천 정보 생성 중 오류 발생: fid={}, uid={}, error={}", fid, uid, e.getMessage());
            return FamilyDashboardResponse.RecommendationSummary.unavailable(
                "추천 정보를 일시적으로 불러올 수 없습니다. 잠시 후 다시 시도해주세요."
            );
        }
    }

    /**
     * 상세한 추천 이유를 한 줄로 축약
     */
    private String generateShortReason(String fullReason) {
        if (fullReason == null || fullReason.trim().isEmpty()) {
            return "가족 구성원 선호도 기반 추천";
        }

        // 첫 번째 문장만 추출 (마침표 기준)
        String[] sentences = fullReason.split("\\.");
        if (sentences.length > 0 && !sentences[0].trim().isEmpty()) {
            String shortReason = sentences[0].trim();
            // 너무 길면 자르기 (50자 제한)
            if (shortReason.length() > 50) {
                shortReason = shortReason.substring(0, 47) + "...";
            }
            return shortReason;
        }

        return "가족 맞춤 추천 요금제";
    }

    /**
     * 대시보드용 상태 메시지 생성
     */
    private String generateDashboardStatusMessage(FamilyPlanRecommendationResponse fullRecommendation) {
        if (fullRecommendation.getRecommendationReason() == null) {
            return "추천 정보가 준비되었습니다.";
        }

        FamilyPlanRecommendationResponse.RecommendationReason reason = 
                fullRecommendation.getRecommendationReason();

        int totalMembers = reason.getTotalMembers();
        int membersWithSurvey = reason.getMembersWithSurvey();

        if (membersWithSurvey == totalMembers) {
            // 모든 구성원이 설문 완료
            return String.format("%s 맞춤 추천이 완료되었습니다!", reason.getFamilyType());
        } else {
            // 일부 구성원 설문 미완료
            int incompleteMembers = totalMembers - membersWithSurvey;
            return String.format("현재 %s으로 분류 (%d명 설문 미완료)", 
                    reason.getFamilyType(), incompleteMembers);
        }
    }

}