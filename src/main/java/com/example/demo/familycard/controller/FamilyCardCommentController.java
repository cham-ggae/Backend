package com.example.demo.familycard.controller;

import com.example.demo.familycard.dto.*;
import com.example.demo.familycard.service.FamilyCardCommentService;
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
 * 가족 메시지 카드 댓글 관련 REST API 컨트롤러
 *
 */
@RestController
@RequestMapping("/family")
@Tag(name = "Family Message Card Comments", description = "가족 메시지 카드 댓글 관리 API")
public class FamilyCardCommentController {

    @Autowired
    private FamilyCardCommentService commentService;

    /**
     * 특정 메시지 카드의 댓글 목록 조회
     */
    @GetMapping("/cards/{fcid}/comments")
    @Operation(
            summary = "메시지 카드 댓글 목록 조회",
            description = "특정 메시지 카드의 모든 댓글을 등록순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "메시지 카드가 존재하지 않음")
    })
    public ResponseEntity<CommentListResponse> getCardComments(
            @Parameter(description = "메시지 카드 ID", required = true, example = "1")
            @PathVariable Long fcid) {

        try {
            CommentListResponse response = commentService.getCardComments(fcid);
            return ResponseEntity.ok(response);

        } catch (FamilyCardCommentService.CommentAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyCardCommentService.CommentServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 댓글 상세 조회
     */
    @GetMapping("/cards/{fcid}/comments/{commentId}")
    @Operation(
            summary = "댓글 상세 조회",
            description = "특정 댓글의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    public ResponseEntity<CommentResponse> getComment(
            @Parameter(description = "메시지 카드 ID", required = true, example = "1")
            @PathVariable Long fcid,

            @Parameter(description = "댓글 ID", required = true, example = "1")
            @PathVariable Long commentId) {

        try {
            CommentResponse response = commentService.getComment(fcid, commentId);
            return ResponseEntity.ok(response);

        } catch (FamilyCardCommentService.CommentAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyCardCommentService.CommentServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 새로운 댓글 작성
     */
    @PostMapping(value = "/cards/{fcid}/comments",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "댓글 작성",
            description = "메시지 카드에 새로운 댓글을 작성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "메시지 카드가 존재하지 않음")
    })
    public ResponseEntity<CommentResponse> createComment(
            @Parameter(description = "메시지 카드 ID", required = true, example = "1")
            @PathVariable Long fcid,

            @Parameter(description = "댓글 작성 요청", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateCommentRequest.class)
                    )
            )
            @Valid @RequestBody CreateCommentRequest request) {

        try {
            CommentResponse response = commentService.createComment(fcid, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (FamilyCardCommentService.CommentAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyCardCommentService.CommentServiceException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 댓글 수정
     */
    @PutMapping(value = "/cards/{fcid}/comments/{commentId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "댓글 수정",
            description = "작성자 본인만 댓글을 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "수정 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    public ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "메시지 카드 ID", required = true, example = "1")
            @PathVariable Long fcid,

            @Parameter(description = "댓글 ID", required = true, example = "1")
            @PathVariable Long commentId,

            @Parameter(description = "댓글 수정 요청", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UpdateCommentRequest.class)
                    )
            )
            @Valid @RequestBody UpdateCommentRequest request) {

        try {
            CommentResponse response = commentService.updateComment(fcid, commentId, request);
            return ResponseEntity.ok(response);

        } catch (FamilyCardCommentService.CommentAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyCardCommentService.CommentServiceException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/cards/{fcid}/comments/{commentId}")
    @Operation(
            summary = "댓글 삭제",
            description = "댓글 작성자 또는 메시지 카드 작성자가 댓글을 삭제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "삭제 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "메시지 카드 ID", required = true, example = "1")
            @PathVariable Long fcid,

            @Parameter(description = "댓글 ID", required = true, example = "1")
            @PathVariable Long commentId) {

        try {
            commentService.deleteComment(fcid, commentId);
            return ResponseEntity.noContent().build();

        } catch (FamilyCardCommentService.CommentAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyCardCommentService.CommentServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 카드의 최근 댓글 조회 (미리보기용)
     */
    @GetMapping("/cards/{fcid}/comments/recent")
    @Operation(
            summary = "최근 댓글 조회",
            description = "특정 메시지 카드의 최근 댓글을 조회합니다. (미리보기용)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "메시지 카드가 존재하지 않음")
    })
    public ResponseEntity<List<CommentResponse>> getRecentComments(
            @Parameter(description = "메시지 카드 ID", required = true, example = "1")
            @PathVariable Long fcid,

            @Parameter(description = "조회할 개수", example = "3")
            @RequestParam(defaultValue = "3") int limit) {

        try {
            // limit 범위 검증
            if (limit < 1 || limit > 10) {
                limit = 3;
            }

            List<CommentResponse> comments = commentService.getRecentComments(fcid, limit);
            return ResponseEntity.ok(comments);

        } catch (FamilyCardCommentService.CommentAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyCardCommentService.CommentServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 가족 구성원별 댓글 작성 통계 조회
     */
    @GetMapping("/comments/statistics/members")
    @Operation(
            summary = "구성원별 댓글 통계 조회",
            description = "현재 사용자 가족의 구성원별 댓글 작성 통계를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "가족 스페이스에 가입되어 있지 않음")
    })
    public ResponseEntity<List<Map<String, Object>>> getCommentStatistics() {

        try {
            List<Map<String, Object>> statistics = commentService.getCommentStatistics();
            return ResponseEntity.ok(statistics);

        } catch (FamilyCardCommentService.CommentServiceException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 메시지 카드별 댓글 수 통계 조회
     */
    @GetMapping("/comments/statistics/cards")
    @Operation(
            summary = "카드별 댓글 수 통계 조회",
            description = "현재 사용자 가족의 메시지 카드별 댓글 수 통계를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "가족 스페이스에 가입되어 있지 않음")
    })
    public ResponseEntity<List<Map<String, Object>>> getCardCommentStatistics() {

        try {
            List<Map<String, Object>> statistics = commentService.getCardCommentStatistics();
            return ResponseEntity.ok(statistics);

        } catch (FamilyCardCommentService.CommentServiceException e) {
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 카드의 댓글 수 조회
     */
    @GetMapping("/cards/{fcid}/comments/count")
    @Operation(
            summary = "특정 카드의 댓글 수 조회",
            description = "특정 메시지 카드의 총 댓글 개수를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "해당 가족의 구성원이 아님"),
            @ApiResponse(responseCode = "404", description = "메시지 카드가 존재하지 않음")
    })
    public ResponseEntity<Map<String, Object>> getCommentCount(
            @Parameter(description = "메시지 카드 ID", required = true, example = "1")
            @PathVariable Long fcid) {

        try {
            int count = commentService.getCommentCount(fcid);
            Map<String, Object> response = Map.of(
                    "fcid", fcid,
                    "commentCount", count
            );
            return ResponseEntity.ok(response);

        } catch (FamilyCardCommentService.CommentAccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (FamilyCardCommentService.CommentServiceException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}