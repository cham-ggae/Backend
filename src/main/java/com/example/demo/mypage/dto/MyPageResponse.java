package com.example.demo.mypage.dto;

import lombok.Data;

import java.util.List;

@Data
public class MyPageResponse {
    private UserInfo userInfo;
    private SurveyResult surveyResult;
    private List<RecommendHistory> recommendHistory;

    @Data
    public static class UserInfo {
        private String name;
        private String profileImage;
        private Long bugId;
    }

    @Data
    public static class SurveyResult {
        private String bugName;
        private String feature;
        private String personality;
    }
    @Data
    public static class RecommendHistory {
        private int planId;
        private String planName;
        private int price;
        private int discountPrice;
        private String benefit;
        private String link;
    }
}
