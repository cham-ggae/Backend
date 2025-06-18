package com.example.demo.familycard.controller;

import com.example.demo.familycard.dto.*;
import com.example.demo.familycard.service.FamilyCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 가족 메시지 카드 관련 REST API 컨트롤러
 *
 * @author 참깨라면팀
 * @since 1.0
 */
@RestController
@RequestMapping("/family-space")
@Tag(name = "Family Message Cards", description = "가족 메시지 카드 관리 API")
public class FamilyCardController {

    @Autowired
    private FamilyCardService familyCardService;

    /**
     * 가족 메시지 카드 목록 조회
     */
    @GetMapping("/cards")
    @Operation(
            summary = "가족 메시지 카드 목록 조회",
            description = "현재 사용자가 속한 가족 스페이스의 모든 메시지 카드를 최신순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "가족 스페이스에 가입되어 있지 않음")
    })
    public ResponseEntity<FamilyCardListResponse> getFamilyCards() {
        try {
            FamilyCardListResponse response = familyCardService.getFamilyCards();
            return ResponseEntity.ok(response);

        } catch (FamilyCardService.FamilyCardServiceException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 메시지 카드 상세 조회
     */
    @GetMapping("/cards/{fcid}")
    @Operation(
            summary = "메시지 카드 상세 조회",
            description = "특정 메시지 카드의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "가족 스페이스에 가입되어 있지 않음"),
            @ApiResponse(responseCode = "404", description = "메시지 카드가 존재하지 않음")
    })
    public ResponseEntity<FamilyCardResponse> getFamilyCard(
            @Parameter(description = "메시지 카드 ID", required = true, example = "1")
            @PathVariable Long fcid) {

        try {
            FamilyCardResponse response = familyCardService.getFamilyCard(fcid);
            return ResponseEntity.ok(response);

        } catch (FamilyCardService.FamilyCardServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 새로운 메시지 카드 작성
     */
    @PostMapping(value = "/cards", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "메시지 카드 작성",
            description = "새로운 메시지 카드를 작성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "카드 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 가족 스페이스에 가입되어 있지 않음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<FamilyCardResponse> createFamilyCard(
            @Parameter(description = "메시지 카드 작성 요청", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateFamilyCardRequest.class)
                    )
            )
            @Valid @RequestBody CreateFamilyCardRequest request) {

        try {
            FamilyCardResponse response = familyCardService.createFamilyCard(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (FamilyCardService.FamilyCardServiceException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 메시지 카드 수정
     */
    @PutMapping(value = "/cards/{fcid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "메시지 카드 수정",
            description = "작성자 본인만 메시지 카드를 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카드 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 가족 스페이스에 가입되어 있지 않음"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "수정 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "메시지 카드가 존재하지 않음")
    })
    public ResponseEntity<FamilyCardResponse> updateFamilyCard(
            @Parameter(description = "메시지 카드 ID", required = true, example = "1")
            @PathVariable Long fcid,

            @Parameter(description = "메시지 카드 수정 요청", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UpdateFamilyCardRequest.class)
                    )
            )
            @Valid @RequestBody UpdateFamilyCardRequest request) {

        try {
            FamilyCardResponse response = familyCardService.updateFamilyCard(fcid, request);
            return ResponseEntity.ok(response);

        } catch (FamilyCardService.FamilyCardAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyCardService.FamilyCardServiceException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 메시지 카드 삭제
     */
    @DeleteMapping("/cards/{fcid}")
    @Operation(
            summary = "메시지 카드 삭제",
            description = "작성자 본인만 메시지 카드를 삭제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "카드 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "삭제 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "메시지 카드가 존재하지 않음")
    })
    public ResponseEntity<Void> deleteFamilyCard(
            @Parameter(description = "메시지 카드 ID", required = true, example = "1")
            @PathVariable Long fcid) {

        try {
            familyCardService.deleteFamilyCard(fcid);
            return ResponseEntity.noContent().build();

        } catch (FamilyCardService.FamilyCardAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyCardService.FamilyCardServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 가족 구성원별 메시지 카드 통계 조회
     */
    @GetMapping("/cards/statistics")
    @Operation(
            summary = "메시지 카드 통계 조회",
            description = "현재 사용자 가족의 구성원별 메시지 카드 작성 통계를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "가족 스페이스에 가입되어 있지 않음")
    })
    public ResponseEntity<List<Map<String, Object>>> getFamilyCardStatistics() {
        try {
            List<Map<String, Object>> statistics = familyCardService.getFamilyCardStatistics();
            return ResponseEntity.ok(statistics);

        } catch (FamilyCardService.FamilyCardServiceException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 최근 메시지 카드 조회 (대시보드용)
     */
    @GetMapping("/cards/recent")
    @Operation(
            summary = "최근 메시지 카드 조회",
            description = "대시보드 표시용 현재 사용자 가족의 최근 메시지 카드를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "가족 스페이스에 가입되어 있지 않음")
    })
    public ResponseEntity<List<FamilyCardResponse>> getRecentFamilyCards(
            @Parameter(description = "조회할 개수", example = "5")
            @RequestParam(defaultValue = "5") int limit) {

        try {
            // limit 범위 검증
            if (limit < 1 || limit > 20) {
                limit = 5; // 기본값으로 설정
            }

            List<FamilyCardResponse> cards = familyCardService.getRecentFamilyCards(limit);
            return ResponseEntity.ok(cards);

        } catch (FamilyCardService.FamilyCardServiceException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용 가능한 이미지 타입 목록 조회
     */
    @GetMapping("/cards/image-types")
    @Operation(
            summary = "이미지 타입 목록 조회",
            description = "메시지 카드에서 사용 가능한 이미지 타입 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<Map<String, String>>> getImageTypes() {
        try {
            List<Map<String, String>> imageTypes = List.of(
                    Map.of("code", "heart", "description", "하트"),
                    Map.of("code", "flower", "description", "꽃"),
                    Map.of("code", "star", "description", "별")
            );

            return ResponseEntity.ok(imageTypes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}