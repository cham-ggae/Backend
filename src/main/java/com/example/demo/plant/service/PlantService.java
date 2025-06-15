package com.example.demo.plant.service;

import com.example.demo.plant.dao.PlantDao;
import com.example.demo.plant.dto.PlantStatusResponseDto;
import com.example.demo.plant.dto.RewardHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantDao plantDao;

    public void createPlant(Long uid, String plantType) {
        Long fid = plantDao.getUserFid(uid);

        if (plantDao.selectFamilyMemberCount(fid) < 2) {
            throw new IllegalArgumentException("구성원 2명 이상 필요");
        }

        if (plantDao.hasUncompletedPlant(fid)) {
            throw new IllegalStateException("기존 식물이 존재함");
        }

        int kid = plantDao.getPlantKindId(plantType);
        plantDao.insertPlant(fid, kid);
    }

    public PlantStatusResponseDto getLatestPlant(Long fid) {
        return plantDao.selectLatestPlantByFid(fid);
    }

    public void claimReward(Long uid) {
        Long fid = plantDao.getUserFid(uid);
        Long pid = plantDao.getLatestPlantId(fid);

        if (!plantDao.isPlantCompleted(pid)) {
            throw new IllegalStateException("아직 완료되지 않은 식물입니다.");
        }

        int rewardId = 1; // fixed reward logic or calculate dynamically
        plantDao.insertRewardLog(uid, fid, pid, rewardId);
        plantDao.markPlantCompleted(pid);
    }

    public List<RewardHistoryDto> getRewardHistory(Long uid) {
        return plantDao.getRewardHistory(uid);
    }
}