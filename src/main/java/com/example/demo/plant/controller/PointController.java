package com.example.demo.plant.controller;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.plant.dto.AddPointRequestDto;
import com.example.demo.plant.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
@Tag(name = "Point System", description = "포인트 및 활동 관련 API")
public class PointController {

    private final PointService pointService;
    private final AuthenticationService authenticationService;

    @PostMapping("/add")
    @Operation(summary = "포인트 적립", description = "지정된 활동 \"attendance\", \"water\", \"nutrient\", \"emotion\", \"quiz\",\"lastleaf\", \"register\",\"survey\"")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "포인트 적립 완료"),
            @ApiResponse(responseCode = "400", description = "중복 활동 또는 활동 불가 상태"),
            @ApiResponse(responseCode = "409", description = "구성원 수 부족 또는 식물 미생성")
    })
    public ResponseEntity<String> addPoint(
            @Parameter(description = "활동 요청 데이터", required = true)
            @RequestBody AddPointRequestDto request) {
        Long uid = authenticationService.getCurrentUserId();
        pointService.addPoint(uid, request.getActivityType());
        return ResponseEntity.ok("포인트 적립 완료");
    }

    @GetMapping("/check-today/{type}")
    @Operation(summary = "오늘 활동 여부 확인", description = "해당 활동을 오늘 이미 수행했는지 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 완료"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<Boolean> checkToday(
            @Parameter(description = "활동 타입 (예: water, attendance 등)", required = true)
            @PathVariable String type) {
        Long uid = authenticationService.getCurrentUserId();
        boolean done = pointService.checkActivityExists(uid, type);
        return ResponseEntity.ok(done);
    }

    @GetMapping("/watered-members/{fid}")
    @Operation(summary = "오늘 물 준 사용자 목록", description = "해당 가족(fid)에서 오늘 물을 준 사용자 ID 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<Long>> getWateredMembers(
            @Parameter(description = "가족 ID", required = true)
            @PathVariable Long fid) {
        List<Long> uids = pointService.getWateredMembers(fid);
        return ResponseEntity.ok(uids);
    }
}