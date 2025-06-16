package com.example.demo.mypage.dto;

import lombok.Data;

@Data
public class RecommendHistoryData {
    private int planId;
    private String planName;
    private int price;
    private int discountPrice;
    private String benefit;
    private String link;
}
