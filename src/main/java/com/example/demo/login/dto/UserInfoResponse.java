package com.example.demo.login.dto;

import lombok.Data;

/**
 * 사용자 정보 조회 응답 DTO
 * 보안상 kakao_accesstoken과 kakao_refreshtoken은 제외
 */
@Data
public class UserInfoResponse {
    private Long uid;
    private Long fid;
    private Long plan_id;
    private Long bug_id;
    private String name;
    private String email;
    private String age;
    private String gender;
    private String survey_date;
    private String join_date;
    private String role;
    private String profile_image;
    
    /**
     * User 엔티티로부터 UserInfoResponse 생성 (토큰 정보 제외)
     */
    public static UserInfoResponse from(User user) {
        UserInfoResponse response = new UserInfoResponse();
        response.setUid(user.getUid());
        response.setFid(user.getFid());
        response.setPlan_id(user.getPlan_id());
        response.setBug_id(user.getBug_id());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAge(user.getAge());
        response.setGender(user.getGender());
        response.setSurvey_date(user.getSurvey_date());
        response.setJoin_date(user.getJoin_date());
        response.setRole(user.getRole());
        response.setProfile_image(user.getProfile_image());
        return response;
    }
} 