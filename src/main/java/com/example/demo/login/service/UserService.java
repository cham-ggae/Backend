package com.example.demo.login.service;

import com.example.demo.login.dao.UserDao;
import com.example.demo.login.dto.AdditionalUserInfoRequest;
import com.example.demo.login.dto.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * 사용자 정보 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserDao userDao;
    private final AuthenticationService authenticationService;

    /**
     * 사용자가 추가 정보 입력이 필요한지 확인
     *
     * @param email 사용자 이메일
     * @return 추가 정보 필요 여부 (true: 필요, false: 불필요)
     */
    public boolean requiresAdditionalInfo(String email) {
        try {
            User user = userDao.findByEmail(email);
            if (user == null) {
                log.warn("사용자를 찾을 수 없습니다. email: {}", email);
                return true; // 사용자가 없으면 추가 정보 필요
            }

            // gender 또는 age가 null이거나 빈 문자열인 경우 추가 정보 필요
            boolean hasGender = user.getGender() != null && !user.getGender().trim().isEmpty();
            boolean hasAge = user.getAge() != null && !user.getAge().trim().isEmpty();

            boolean requiresInfo = !hasGender || !hasAge;

            log.debug("사용자 {} 추가 정보 필요 여부: {} (gender: {}, age: {})",
                    email, requiresInfo, user.getGender(), user.getAge());

            return requiresInfo;

        } catch (SQLException e) {
            log.error("사용자 정보 조회 중 오류 발생. email: {}", email, e);
            return true; // 오류 발생 시 안전하게 추가 정보 필요로 처리
        }
    }

    /**
     * 사용자 추가 정보 업데이트
     *
     * @param request 성별, 나이대 정보
     * @throws SQLException DB 업데이트 실패 시
     * @throws AuthenticationService.AuthenticationException 인증되지 않은 사용자인 경우
     */
    @Transactional
    public void updateAdditionalInfo(AdditionalUserInfoRequest request) throws SQLException {
        // 현재 인증된 사용자 정보 가져오기
        String currentUserEmail = authenticationService.getCurrentUserEmail();

        log.info("사용자 추가 정보 업데이트 시작. email: {}, gender: {}, age: {}",
                currentUserEmail, request.getGender(), request.getAge());

        // 사용자 정보 업데이트
        userDao.updateUserInfo(currentUserEmail, request.getAge(), request.getGender());

        log.info("사용자 추가 정보 업데이트 완료. email: {}", currentUserEmail);
    }

    /**
     * 현재 사용자의 추가 정보 필요 여부 확인 (인증된 사용자용)
     *
     * @return 추가 정보 필요 여부
     */
    public boolean currentUserRequiresAdditionalInfo() {
        try {
            String currentUserEmail = authenticationService.getCurrentUserEmail();
            return requiresAdditionalInfo(currentUserEmail);
        } catch (AuthenticationService.AuthenticationException e) {
            log.error("인증되지 않은 사용자의 추가 정보 필요 여부 확인 시도", e);
            return true;
        }
    }
}