package com.example.demo.plant.service;

import com.example.demo.plant.dao.PlantDao;
import com.example.demo.plant.dto.PlantResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantDao plantDao;

    // 식물 상태 조회
    public PlantResponseDto getLatestPlantByFid(int fid) {
        return plantDao.findLatestPlantByFamilyId(fid);
    }

    // 새싹 생성
    public void createPlant(int fid, String type) {
        plantDao.insertPlant(fid, type);
    }

    // 성장 완료 여부 체크
    public boolean isCompleted(int fid) {
        return plantDao.isPlantCompleted(fid);
    }

    // 성장 완료 처리 (내부 호출용)
    public void completePlant(int pid) {
        plantDao.completePlant(pid);
    }
}