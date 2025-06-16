package com.example.demo.plant.controller;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.plant.service.NutrientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nutrients")
@RequiredArgsConstructor
@Tag(name = "Nutrient", description = "영양제 관련 API")
public class NutrientController {

    private final NutrientService nutrientService;
    private final AuthenticationService authService;

    @GetMapping("/stock")
    @Operation(
            summary = "현재 가족의 영양제 수량 조회",
            description = "로그인한 사용자의 가족 스페이스에서 현재 보유 중인 영양제 수량을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "가족 정보가 존재하지 않음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<Integer> getNutrientStock() {
        Long uid = authService.getCurrentUserId();
        int stock = nutrientService.getNutrientStockByUid(uid);
        return ResponseEntity.ok(stock);
    }
}