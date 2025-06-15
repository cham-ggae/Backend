package com.example.demo.plant.websocket.dto;

import lombok.Data;

@Data
public class WaterEventData {
    private Long fid;
    private Long uid;
    private String name;
    private String avatarUrl;// = Users.profile_image 와 매핑
}