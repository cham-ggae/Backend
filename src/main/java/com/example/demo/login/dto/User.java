package com.example.demo.login.dto;

import lombok.Data;

@Data
public class User {
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
    private String kakao_accesstoken;
    private String kakao_refreshtoken;
    private String role;
    private String profile_image;
}
