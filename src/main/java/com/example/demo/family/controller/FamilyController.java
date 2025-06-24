package com.example.demo.family.controller;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.family.dto.*;
import com.example.demo.family.service.FamilyService;
import com.example.demo.family.service.FamilyPlanRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 가족 스페이스 관련 REST API 컨트롤러
 * JWT 기반 인증을 통한 보안 강화 버전
 *
 */
@RestController
@RequestMapping("/family")
@Tag(name = "Family Space", description = "가족 스페이스 관리 API")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private FamilyPlanRecommendationService familyPlanRecommendationService;

    /**
     * 새로운 가족 스페이스 생성
     * JWT 토큰에서 사용자 정보를 자동으로 추출하여 처리
     */
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "가족 스페이스 생성",
            description = "새로운 가족 스페이스를 생성하고 요청자를 첫 번째 구성원으로 등록합니다. JWT 토큰으로 사용자를 식별합니다. combitype \"투게더 결합\", \"참쉬운 가족 결합\", "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가족 스페이스 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "409", description = "이미 가족에 속해있음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<CreateFamilyResponse> createFamilySpace(
            @Parameter(description = "가족 생성 요청 정보", required = true)
            @RequestBody CreateFamilyRequest request) {

        try {
            // JWT 토큰에서 현재 인증된 사용자 ID 획득
            Long currentUserId = authService.getCurrentUserId();

            CreateFamilyResponse response = familyService.createFamilySpace(currentUserId, request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                // 이미 가족에 속해있는 경우 409 Conflict 반환
                if (response.getMessage().contains("이미 가족")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                }
                return ResponseEntity.badRequest().body(response);
            }

        } catch (AuthenticationService.AuthenticationException e) {
            CreateFamilyResponse errorResponse = CreateFamilyResponse.failure("인증이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            CreateFamilyResponse errorResponse = CreateFamilyResponse.failure("서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    /**
     * 초대 코드로 가족 스페이스 참여
     * JWT 토큰에서 사용자 정보를 자동으로 추출하여 처리
     */
    @PostMapping(
        value = "/join",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "가족 스페이스 참여",
            description = "초대 코드를 통해 기존 가족 스페이스에 참여합니다. JWT 토큰으로 사용자를 식별합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가족 참여 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 초대 코드"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "409", description = "이미 가족에 속해있음 또는 가족 정원 초과")
    })
    public ResponseEntity<CreateFamilyResponse> joinFamilySpace(
            @Parameter(description = "가족 참여 요청 정보", required = true)
            @RequestBody JoinFamilyRequest request) {

        try {
            // JWT 토큰에서 현재 인증된 사용자 ID 획득
            Long currentUserId = authService.getCurrentUserId();

            CreateFamilyResponse response = familyService.joinFamilySpace(currentUserId, request.getInviteCode());

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                // 가족 관련 제약 조건 위반 시 409 Conflict 반환
                if (response.getMessage().contains("이미") || response.getMessage().contains("최대")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                }
                return ResponseEntity.badRequest().body(response);
            }

        } catch (AuthenticationService.AuthenticationException e) {
            CreateFamilyResponse errorResponse = CreateFamilyResponse.failure("인증이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            CreateFamilyResponse errorResponse = CreateFamilyResponse.failure("서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    /**
     * 초대 코드 유효성 검증
     * 인증 없이도 호출 가능 (가족 정보 미리보기용)
     */
    @GetMapping("/invite/{inviteCode}")
    @Operation(
            summary = "초대 코드 검증",
            description = "초대 코드의 유효성을 검증하고 가족 정보를 반환합니다. 인증 없이도 호출 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검증 완료 (유효/무효 모두 포함)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<InviteCodeValidationResponse> validateInviteCode(
            @Parameter(description = "초대 코드", required = true, example = "A1B2C3")
            @PathVariable String inviteCode) {

        try {
            InviteCodeValidationResponse response = familyService.validateInviteCode(inviteCode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            InviteCodeValidationResponse errorResponse =
                    InviteCodeValidationResponse.failure("서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================
    // 5. 새 초대 코드 생성
    // ========================================

    /**
     * 새로운 초대 코드 생성
     * 가족 구성원만 초대 코드를 갱신할 수 있음
     */
    @PostMapping("/{fid}")
    @Operation(
            summary = "새 초대 코드 생성",
            description = "기존 초대 코드를 새로운 코드로 갱신합니다. 해당 가족의 구성원만 실행 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "새 초대 코드 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "가족 스페이스가 존재하지 않음")
    })
    public ResponseEntity<String> generateNewInviteCode(
            @Parameter(description = "가족 스페이스 ID", required = true, example = "1")
            @PathVariable Long fid) {

        try {
            // JWT 토큰에서 현재 인증된 사용자 ID 획득
            Long currentUserId = authService.getCurrentUserId();

            String newInviteCode = familyService.generateNewInviteCode(fid, currentUserId);
            return ResponseEntity.ok(newInviteCode);

        } catch (AuthenticationService.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (FamilyService.FamilyAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyService.FamilyServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // 6. 가족 이름 변경
    // ========================================

    /**
     * 가족 이름 변경
     * 가족 구성원만 가족 이름을 변경할 수 있음
     */
    @PostMapping(
        value = "/{fid}/name",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "가족 이름 변경",
            description = "가족 스페이스의 이름을 변경합니다. 해당 가족의 구성원만 실행 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가족 이름 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "가족 스페이스가 존재하지 않음")
    })
    public ResponseEntity<UpdateFamilyNameResponse> updateFamilyName(
            @Parameter(description = "가족 스페이스 ID", required = true, example = "1")
            @PathVariable Long fid,
            @Parameter(description = "가족 이름 변경 요청 정보", required = true)
            @RequestBody UpdateFamilyNameRequest request) {

        try {
            // JWT 토큰에서 현재 인증된 사용자 ID 획득
            Long currentUserId = authService.getCurrentUserId();

            UpdateFamilyNameResponse response = familyService.updateFamilyNameWithResponse(fid, currentUserId, request.getName());

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (AuthenticationService.AuthenticationException e) {
            UpdateFamilyNameResponse errorResponse = UpdateFamilyNameResponse.failure("인증이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            UpdateFamilyNameResponse errorResponse = UpdateFamilyNameResponse.failure("서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================
    // 7. 가족 탈퇴
    // ========================================

    /**
     * 가족 스페이스에서 나가기
     * 마지막 구성원인 경우 가족 스페이스가 자동으로 삭제됨
     */
    @DeleteMapping("/{fid}")
    @Operation(
            summary = "가족 탈퇴",
            description = "현재 가족 스페이스에서 탈퇴합니다. 마지막 구성원인 경우 가족이 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "가족 스페이스가 존재하지 않음")
    })
    public ResponseEntity<Void> leaveFamilySpace(
            @Parameter(description = "가족 스페이스 ID", required = true, example = "1")
            @PathVariable Long fid) {

        try {
            // JWT 토큰에서 현재 인증된 사용자 ID 획득
            Long currentUserId = authService.getCurrentUserId();

            familyService.leaveFamilySpace(currentUserId, fid);
            return ResponseEntity.ok().build();

        } catch (AuthenticationService.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (FamilyService.FamilyAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyService.FamilyServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // 8. 가족 요금제 추천
    // ========================================

    /**
     * 가족 구성원 설문 결과 기반 요금제 추천
     * 가족 구성원들의 설문 결과를 종합하여 최적의 요금제를 추천
     */
    @PostMapping(
        value = "/{fid}/plan-recommendation",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "가족 요금제 추천",
            description = "가족 구성원들의 설문 결과를 바탕으로 가족에게 맞는 요금제를 추천합니다. 해당 가족의 구성원만 실행 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요금제 추천 성공"),
            @ApiResponse(responseCode = "400", description = "설문 완료 구성원 없음 등"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "가족 스페이스가 존재하지 않음")
    })
    public ResponseEntity<FamilyPlanRecommendationResponse> recommendFamilyPlans(
            @Parameter(description = "가족 스페이스 ID", required = true, example = "1")
            @PathVariable Long fid) {

        try {
            // JWT 토큰에서 현재 인증된 사용자 ID 획득
            Long currentUserId = authService.getCurrentUserId();

            FamilyPlanRecommendationResponse response = 
                    familyPlanRecommendationService.recommendFamilyPlans(fid, currentUserId);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (AuthenticationService.AuthenticationException e) {
            FamilyPlanRecommendationResponse errorResponse = 
                    FamilyPlanRecommendationResponse.failure("인증이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            FamilyPlanRecommendationResponse errorResponse = 
                    FamilyPlanRecommendationResponse.failure("서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================
    // 9. 가족 정보 조회
    // ========================================

    /**
     * 현재 사용자가 속한 가족 정보 조회 (간소화된 식물 정보 + 추천 정보 포함)
     */
    @GetMapping()
    @Operation(
            summary = "내 가족 정보 조회 (간소화된 식물 정보 + 추천 정보 포함)",
            description = "현재 로그인한 사용자가 속한 가족의 기본 정보, 식물의 레벨/종류/생성 여부, 그리고 간소화된 요금제 추천 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "가족에 속해있지 않음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<FamilyDashboardResponse> getMyFamilyWithPlant() {
        try {
            Long currentUserId = authService.getCurrentUserId();
            Long userFamilyId = familyService.getUserCurrentFamilyId(currentUserId);

            if (userFamilyId == null) {
                return ResponseEntity.noContent().build();
            }

            FamilyDashboardResponse response = familyService.getFamilyDashboardWithPlant(userFamilyId, currentUserId);
            return ResponseEntity.ok(response);

        } catch (AuthenticationService.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 가족 스페이스 대시보드 정보 조회 (간소화된 식물 정보 + 추천 정보 포함)
     */
    @GetMapping("/{fid}")
    @Operation(
            summary = "가족 대시보드 조회 (간소화된 식물 정보 + 추천 정보 포함)",
            description = "가족 스페이스의 기본 정보, 식물의 레벨/종류/생성 여부, 그리고 간소화된 요금제 추천 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "가족 스페이스가 존재하지 않음")
    })
    public ResponseEntity<FamilyDashboardResponse> getFamilyDashboardWithPlant(
            @Parameter(description = "가족 스페이스 ID", required = true, example = "1")
            @PathVariable Long fid) {
        try {
            Long currentUserId = authService.getCurrentUserId();
            FamilyDashboardResponse response = familyService.getFamilyDashboardWithPlant(fid, currentUserId);
            return ResponseEntity.ok(response);

        } catch (AuthenticationService.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (FamilyService.FamilyAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (FamilyService.FamilyServiceException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}