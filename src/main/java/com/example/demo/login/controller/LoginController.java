package com.example.demo.login.controller;

import com.example.demo.login.dto.AdditionalUserInfoRequest;
import com.example.demo.login.dto.KakaoLogoutRes;
import com.example.demo.login.dto.KakaoUserInfo;
import com.example.demo.login.dto.User;
import com.example.demo.login.dto.UserInfoResponse;
import com.example.demo.login.service.AuthenticationService;
import com.example.demo.login.service.KakaoLoginService;
import com.example.demo.login.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 로그인 관련 컨트롤러.
 *
 * <p>카카오 로그인, 로그아웃, 액세스 토큰 갱신 API 엔드포인트를 제공한다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "로그인", description = "카카오 OAuth 기반 로그인 및 사용자 정보 관리 API")
public class LoginController {

    private final KakaoLoginService kakaoLoginService;
    private final UserService userService;

    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    // 리프레시 토큰 만료 기한 14일
    private static final long REFRESH_VALIDITY = 1000L * 60 * 60 * 24 * 14;

    @Operation(
            summary = "카카오 OAuth 인증 페이지로 리다이렉트",
            description = "카카오 로그인 인증 페이지로 사용자를 리다이렉트합니다."
    )
    @GetMapping("/authorize")
    public void redirectToKakaoAuth(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";

        response.sendRedirect(kakaoAuthUrl);
    }

    /**
     * 카카오 로그인 요청 처리.
     *
     * @param body            JSON으로 전달된 인가 코드 ({@code {"code":"..."}})
     * @param servletResponse 리프레시 토큰을 쿠키에 담기 위한 응답 객체
     * @return 이메일, 닉네임과 함께 Authorization 헤더에 액세스 토큰이 설정된 응답
     */
    @Operation(
            summary = "카카오 로그인 처리",
            description = "카카오에서 받은 인가 코드를 사용하여 로그인을 처리하고 JWT 토큰을 발급합니다. " +
                    "추가 정보(성별, 나이대) 입력이 필요한지 여부도 함께 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "email": "user@example.com",
                                        "nickname": "사용자닉네임",
                                        "requiresAdditionalInfo": true
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 인가 코드"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/kakao")
    public ResponseEntity<Map<String,Object>> loginWithKakao(
            @Parameter(description = "카카오 인가 코드", required = true,
                    example = "{\"code\":\"authorization_code_from_kakao\"}")
            @RequestBody Map<String,String> body,
            HttpServletResponse servletResponse
    ) throws SQLException {
        String code = body.get("code");
        log.debug(code);

        // 프론트로부터 인가 코드 받아서 토큰 받아온 후 유저 정보까지 받아오는 로직 여기서 실행함
        KakaoUserInfo info = kakaoLoginService.handleKakaoLogin(code);

        // 받아온 refreshtoken을 쿠키로 보냄
        Cookie cookie = new Cookie("refreshToken", info.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (REFRESH_VALIDITY / 1000));
        servletResponse.addCookie(cookie);

        //받아온 accessToken은 authorization 헤더에 집어넣음
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(info.getAccessToken());

        // 추가 정보 필요 여부 확인
        boolean requiresAdditionalInfo = userService.requiresAdditionalInfo(info.getEmail());

        // 나머지 정보는 json 형식으로 파싱함
        Map<String,Object> result = new HashMap<>();
        result.put("email", info.getEmail());
        result.put("nickname", info.getNickname());
        result.put("requiresAdditionalInfo", requiresAdditionalInfo);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(result);
    }

    /**
     * 사용자 추가 정보 업데이트
     *
     * @param request 성별, 나이대 정보
     * @return 업데이트 결과
     */
    @Operation(
            summary = "사용자 추가 정보 업데이트",
            description = "로그인한 사용자의 추가 정보(성별, 나이대)를 업데이트합니다. " +
                    "이 정보는 사용자가 처음 로그인한 후 필수로 입력해야 하는 정보입니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 업데이트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                    "success": true,
                                    "message": "사용자 정보가 성공적으로 업데이트되었습니다."
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                    "success": false,
                                    "message": "잘못된 요청 데이터입니다.",
                                    "error": "유효성 검증 실패"
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                    "success": false,
                                    "message": "인증이 필요합니다."
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                    "success": false,
                                    "message": "사용자 정보 업데이트에 실패했습니다.",
                                    "error": "데이터베이스 연결 오류"
                                }
                                """
                            )
                    )
            )
    })
    @PostMapping(value = "/user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateAdditionalInfo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사용자 추가 정보 (성별, 나이대)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdditionalUserInfoRequest.class),
                            examples = @ExampleObject(
                                    name = "사용자 추가 정보 예시",
                                    value = """
                                {
                                    "gender": "female",
                                    "age": "20~29"
                                }
                                """
                            )
                    )
            )
            @Valid @RequestBody AdditionalUserInfoRequest request) {

        try {
            userService.updateAdditionalInfo(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "사용자 정보가 성공적으로 업데이트되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 추가 정보 업데이트 실패", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "사용자 정보 업데이트에 실패했습니다.");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * 현재 로그인된 사용자 정보 조회
     *
     * @return 현재 로그인된 사용자 정보 (액세스 토큰 제외)
     */
    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "JWT 토큰을 통해 현재 로그인된 사용자의 정보를 조회합니다. " +
                    "보안상 카카오 액세스 토큰과 리프레시 토큰은 응답에 포함되지 않습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "uid": 1,
                                        "fid": 1,
                                        "plan_id": null,
                                        "bug_id": null,
                                        "name": "김홍길동",
                                        "email": "user@example.com",
                                        "age": "20~29",
                                        "gender": "female",
                                        "survey_date": "2024-01-01",
                                        "join_date": "2024-01-01",
                                        "role": "USER",
                                        "profile_image": "https://example.com/profile.jpg"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "success": false,
                                        "message": "인증이 필요합니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "success": false,
                                        "message": "사용자 정보 조회에 실패했습니다.",
                                        "error": "데이터베이스 연결 오류"
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUserInfo() {
        try {
            User user = userService.getCurrentUserInfo();
            UserInfoResponse response = UserInfoResponse.from(user);
            
            log.debug("사용자 정보 조회 성공. uid: {}, email: {}", user.getUid(), user.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationService.AuthenticationException e) {
            log.warn("인증되지 않은 사용자의 정보 조회 시도: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "인증이 필요합니다.");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "사용자 정보 조회에 실패했습니다.");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 카카오 로그아웃 요청 처리.
     *
     * @param authorization Authorization 헤더에 담긴 액세스 토큰
     * @param response      로그아웃 시 쿠키 삭제 등을 위한 응답 객체
     * @return 로그아웃 결과를 담은 {@link KakaoLogoutRes} DTO
     */
    @Operation(
            summary = "카카오 로그아웃",
            description = "카카오 로그아웃을 처리하고 리프레시 토큰 쿠키를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> kakaoLogout(
            @Parameter(description = "Bearer 토큰", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authorization,
            HttpServletResponse response) {
        // 로그아웃 요청 함
        KakaoLogoutRes result = kakaoLoginService.handleKakaoLogout(authorization, response);
        // 응답은 KakaoLogoutRes dto 참고
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "액세스 토큰 갱신",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다. " +
                    "리프레시 토큰은 HttpOnly 쿠키로 전송되어야 합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "message": "토큰 재발급 성공",
                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "리프레시 토큰이 만료되었거나 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "error": "Refresh Token이 만료되었습니다. 다시 로그인해 주세요."
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return kakaoLoginService.refreshToken(request, response);
    }
}