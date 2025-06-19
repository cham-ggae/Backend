package com.example.demo.plant.controller;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.plant.dto.*;
import com.example.demo.plant.service.PlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

/**
 * 식물 및 보상 관련 API 컨트롤러
 * - 식물 생성 / 조회 / 보상 수령 / 보상 이력 조회
 */
@RestController
@RequestMapping("/plants")
@RequiredArgsConstructor
@Tag(name = "Plant", description = "식물 및 보상 관련 API")
public class PlantController {

    private final PlantService plantService;
    private final AuthenticationService authService;

    /**
     * 새싹 생성
     * @param request plantType (flower/tree)
     * @return 성공 메시지
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // 🔄 multipart/form-data 지원
    @Operation(summary = "새 식물 생성", description = "새싹을 생성합니다. 조건: 가족 구성원 ≥ 2명 && (기존 식물 없음 또는 완료된 상태) / flower ,tree 선택")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "새싹 생성 성공"),
            @ApiResponse(responseCode = "400", description = "가족 구성원이 2명 미만이거나 입력 값 오류"),
            @ApiResponse(responseCode = "409", description = "기존 식물이 아직 완료되지 않음")
    })
    public ResponseEntity<String> createPlant( @ModelAttribute CreatePlantRequestDto request) {
        Long uid = authService.getCurrentUserId();
        plantService.createPlant(uid, request.getPlantType()); // flower or tree
        return ResponseEntity.ok("새싹 생성 완료");
    }

    /**
     * 가족 ID로 최신 식물 상태 조회
     * @param fid 가족 공간 ID
     * @return 식물 상태 DTO
     */
    @GetMapping("/{fid}")
    @Operation(summary = "식물 상태 조회", description = "가족 식물의 상태를 조회합니다. 최신 created_at 식물 기준으로 반환합니다.")
    public ResponseEntity<PlantStatusResponseDto> getPlant(@PathVariable Long fid) {
        return ResponseEntity.ok(plantService.getLatestPlant(fid));
    }

    /**
     * 현재 사용자 기준 보상 수령
     * @return 완료 메시지
     */
    @PostMapping("/claim-reward")
    @Operation(summary = "보상 수령", description = "성장 완료된 식물에 대해 보상을 수령합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "보상 수령 성공"),
            @ApiResponse(responseCode = "409", description = "식물이 완료되지 않았거나 이미 보상을 수령함")
    })
    public ResponseEntity<RewardHistoryDto> claimReward() {
        Long uid = authService.getCurrentUserId();
        RewardHistoryDto result = plantService.claimReward(uid);
        return ResponseEntity.ok(result);
    }

    /**
     * 현재 사용자 기준 보상 수령 내역 조회
     * @return 보상 이력 리스트
     */
    @GetMapping("/rewards/history")
    @Operation(summary = "보상 수령 내역 조회", description = "현재 로그인한 사용자의 보상 수령 이력을 조회합니다.")
    public ResponseEntity<List<RewardHistoryDto>> getMyRewardHistory() {
        Long uid = authService.getCurrentUserId();
        return ResponseEntity.ok(plantService.getRewardHistory(uid));
    }
}