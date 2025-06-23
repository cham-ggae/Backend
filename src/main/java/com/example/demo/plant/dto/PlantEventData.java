package com.example.demo.plant.dto;

import lombok.Data;

@Data
public class PlantEventData {
    private String type;
    private Long fid;
    private Long uid;
    private String name;
    private String avatarUrl;
    private int level;
    private int experiencePoint;
    private int expThreshold;
    private boolean isLevelUp;
}
