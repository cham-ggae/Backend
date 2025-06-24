package com.example.demo.login.service;

import com.example.demo.login.dto.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 인증된 사용자 정보를 제공하는 서비스
 * SecurityContext에서 현재 로그인한 사용자 정보를 추출
 */
@Service
public class AuthenticationService {

    /**
     * 현재 인증된 사용자 정보 조회
     *
     * @return 현재 로그인한 사용자 정보
     * @throws AuthenticationException 인증되지 않은 사용자인 경우
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 🔍 Authentication 객체가 null인지 확인
        System.out.println("[DEBUG] authentication = " + authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("[DEBUG] 인증 실패 - authentication is null or not authenticated");
            throw new AuthenticationException("인증되지 않은 사용자입니다.");
        }

        Object principal = authentication.getPrincipal();

        // 🔍 Principal의 실제 타입 출력
        System.out.println("[DEBUG] principal class = " + principal.getClass().getName());
        System.out.println("[DEBUG] principal value = " + principal);

        if (!(principal instanceof User)) {
            System.out.println("[DEBUG] principal이 User가 아님");
            throw new AuthenticationException("잘못된 인증 정보입니다.");
        }

        return (User) principal;
    }


    /**
     * 현재 인증된 사용자의 ID 조회
     *
     * @return 현재 로그인한 사용자의 uid
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getUid();
    }

    /**
     * 현재 인증된 사용자의 이메일 조회
     *
     * @return 현재 로그인한 사용자의 이메일
     */
    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * 현재 인증된 사용자의 이름 조회
     *
     * @return 현재 로그인한 사용자의 이름
     */
    public String getCurrentUserName() {
        return getCurrentUser().getName();
    }

    /**
     * 사용자 인증 상태 확인
     *
     * @return 인증된 상태면 true, 아니면 false
     */
    public boolean isAuthenticated() {
        try {
            getCurrentUser();
            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }


    /**
     * 인증 관련 예외 클래스
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }


    }

}