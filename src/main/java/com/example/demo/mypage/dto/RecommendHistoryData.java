package com.example.demo.mypage.dto;

import lombok.Data;

@Data
/**
 * RecommendHistoryData 클래스입니다.
 */

//첫번째 추천 요금제
public class RecommendHistoryData {
    private int planId;
    private String planName;
    private int price;
    private int discountPrice;
    private String benefit;
    private String link;
}
