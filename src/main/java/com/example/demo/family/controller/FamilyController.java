package com.example.demo.family.controller;

import com.example.demo.family.dto.*;
import com.example.demo.family.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 가족 스페이스 관련 REST API 컨트롤러
 *
 * @author 참깨라면팀
 * @since 1.0
 */
@RestController
@RequestMapping("/api/family-space")
@Tag(name = "Family Space", description = "가족 스페이스 관리 API")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    // ========================================
    // 1. 가족 스페이스 생성
    // ========================================

    /**
     * 새로운 가족 스페이스 생성
     */
    @PostMapping
    @Operation(
            summary = "가족 스페이스 생성",
            description = "새로운 가족 스페이스를 생성하고 요청자를 첫 번째 구성원으로 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가족 스페이스 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<CreateFamilyResponse> createFamilySpace(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("X-User-Id") Integer uid,

            @Parameter(description = "가족 생성 요청 정보", required = true)
            @RequestBody CreateFamilyRequest request) {

        try {
            CreateFamilyResponse response = familyService.createFamilySpace(uid, request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            CreateFamilyResponse errorResponse = CreateFamilyResponse.failure("서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================
    // 2. 가족 스페이스 참여 (초대 코드)
    // ========================================

    /**
     * 초대 코드로 가족 스페이스 참여
     */
    @PostMapping("/join")
    @Operation(
            summary = "가족 스페이스 참여",
            description = "초대 코드를 통해 기존 가족 스페이스에 참여합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가족 참여 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 초대 코드"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "409", description = "이미 가족에 속해있음")
    })
    public ResponseEntity<CreateFamilyResponse> joinFamilySpace(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("X-User-Id") Integer uid,

            @Parameter(description = "가족 참여 요청 정보", required = true)
            @RequestBody JoinFamilyRequest request) {

        try {
            CreateFamilyResponse response = familyService.joinFamilySpace(uid, request.getInviteCode());

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            CreateFamilyResponse errorResponse = CreateFamilyResponse.failure("서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================
    // 3. 가족 대시보드 조회
    // ========================================

    /**
     * 가족 스페이스 대시보드 정보 조회
     */
    @GetMapping("/{fid}")
    @Operation(
            summary = "가족 대시보드 조회",
            description = "가족 스페이스의 모든 정보(구성원, 할인 정보 등)를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "가족 스페이스가 존재하지 않음")
    })
    public ResponseEntity<FamilyDashboardResponse> getFamilyDashboard(
            @Parameter(description = "가족 스페이스 ID", required = true, example = "1")
            @PathVariable Integer fid,

            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("X-User-Id") Integer uid) {

        try {
            FamilyDashboardResponse response = familyService.getFamilyDashboard(fid, uid);
            return ResponseEntity.ok(response);

        } catch (FamilyService.FamilyAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyService.FamilyServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // 4. 초대 코드 검증
    // ========================================

    /**
     * 초대 코드 유효성 검증
     */
    @GetMapping("/invite/{inviteCode}/validate")
    @Operation(
            summary = "초대 코드 검증",
            description = "초대 코드의 유효성을 검증하고 가족 정보를 반환합니다."
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
     */
    @PostMapping("/{fid}/invite-code")
    @Operation(
            summary = "새 초대 코드 생성",
            description = "기존 초대 코드를 새로운 코드로 갱신합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "새 초대 코드 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "가족 스페이스가 존재하지 않음")
    })
    public ResponseEntity<String> generateNewInviteCode(
            @Parameter(description = "가족 스페이스 ID", required = true, example = "1")
            @PathVariable Integer fid,

            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("X-User-Id") Integer uid) {

        try {
            String newInviteCode = familyService.generateNewInviteCode(fid, uid);
            return ResponseEntity.ok(newInviteCode);

        } catch (FamilyService.FamilyAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyService.FamilyServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // 6. 가족 탈퇴
    // ========================================

    /**
     * 가족 스페이스에서 나가기
     */
    @DeleteMapping("/{fid}/leave")
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
            @PathVariable Integer fid,

            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("X-User-Id") Integer uid) {

        try {
            familyService.leaveFamilySpace(uid, fid);
            return ResponseEntity.ok().build();

        } catch (FamilyService.FamilyAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyService.FamilyServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // 7. 현재 사용자의 가족 정보 조회
    // ========================================

    /**
     * 현재 사용자가 속한 가족 정보 조회
     */
    @GetMapping("/my-family")
    @Operation(
            summary = "내 가족 정보 조회",
            description = "현재 로그인한 사용자가 속한 가족의 기본 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "가족에 속해있지 않음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<FamilyDashboardResponse> getMyFamily(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("X-User-Id") Integer uid) {

        try {
            // 사용자가 속한 가족 ID 조회
            Integer userFamilyId = familyService.getUserCurrentFamilyId(uid);

            if (userFamilyId == null) {
                return ResponseEntity.noContent().build(); // 가족에 속해있지 않음
            }

            FamilyDashboardResponse response = familyService.getFamilyDashboard(userFamilyId, uid);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}