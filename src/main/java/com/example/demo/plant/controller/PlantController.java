package com.example.demo.plant.controller;

import com.example.demo.plant.dto.PlantResponseDto;
import com.example.demo.plant.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plants")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;

    // 식물 생성
    @PostMapping
    public String createPlant(@RequestParam int fid, @RequestParam String type) {
        plantService.createPlant(fid, type);
        return "식물이 생성되었습니다.";
    }

    // 식물 상태 조회
    @GetMapping("/{fid}")
    public PlantResponseDto getPlantStatus(@PathVariable int fid) {
        return plantService.getLatestPlantByFid(fid);
    }

    // 보상 수령 처리
    @PostMapping("/claim-reward")
    public String claimReward(@RequestParam int fid, @RequestParam int pid) {
        if (!plantService.isCompleted(fid)) {
            return "아직 성장 완료되지 않았습니다.";
        }
        plantService.completePlant(pid); // 보상 처리 등 별도 service 로직 추가 가능
        return "보상이 지급되었습니다.";
    }
}