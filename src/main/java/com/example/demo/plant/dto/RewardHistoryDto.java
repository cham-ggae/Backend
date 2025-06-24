package com.example.demo.plant.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 보상 수령 내역 DTO
 * - 보상 이름, 설명, 수령 시간
 */
@Getter
@Setter
public class RewardHistoryDto {
    private Long rewardLogId;
    private int rewardId;
    private String rewardName;
    private String description;
    private String receivedAt;
    private boolean isUsed;
}