package com.example.demo.plant.service;

import com.example.demo.plant.dao.PlantDao;
import com.example.demo.plant.dto.PlantStatusResponseDto;
import com.example.demo.plant.dto.RewardHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import static com.example.demo.plant.exception.PlantExceptions.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantDao plantDao;
    private final PointService pointService;
    // 1. 새로운 식물 반환
    public void createPlant(Long uid, String plantType) {
        Long fid = plantDao.getUserFid(uid);

        if (plantDao.selectFamilyMemberCount(fid) < 2) {
            throw new NotEnoughFamilyMembersException("가족 구성원은 최소 2명 이상이어야 합니다.");
        }

        if (plantDao.hasUncompletedPlant(fid)) {
            throw new UncompletedPlantExistsException("기존 식물이 아직 완료되지 않았습니다.");
        }

        int kid = plantDao.getPlantKindId(plantType);
        plantDao.insertPlant(fid, kid);
    }

    // 2. 식물 조회 값에 대한 반환 // dto 참고
    public PlantStatusResponseDto getLatestPlant(Long fid) {
        PlantStatusResponseDto dto = plantDao.selectLatestPlantByFid(fid);

        Long pid = plantDao.getLatestPlantId(fid);
        int memberCount = plantDao.selectFamilyMemberCount(fid);

        int level = dto.getLevel();
        int threshold = pointService.getExpThreshold(memberCount, level);

        dto.setExpThreshold(threshold);
        return dto;
    }

    //완료된 식물에 대해 어떤 보상을 줄지 결정하는 로직
    private int calculateRewardId(Long fid, Long pid) {
        // 예: 단순 랜덤 (후에 레벨, 종류, 활동 횟수 기반으로 확장 가능)
        List<Integer> rewardIds = List.of(1, 2, 3, 4);
        return rewardIds.get((int) (Math.random() * rewardIds.size()));
    }
    // 3. 완료된 식물에 대한 보상
    public RewardHistoryDto claimReward(Long uid) {
        Long fid = plantDao.getUserFid(uid);
        Long pid = plantDao.getLatestPlantId(fid);

        if (!plantDao.isPlantCompleted(pid)) {
            throw new PlantNotCompletedException("아직 완료되지 않은 식물입니다.");
        }
        // 보상은 가족당 단 한번!!
        if (plantDao.hasAlreadyClaimedReward(uid, pid)) {
            throw new RewardAlreadyClaimedException("이미 보상을 수령한 식물입니다.");
        }

        int rewardId = calculateRewardId(fid, pid);
        plantDao.insertRewardLog(uid, fid, pid, rewardId);
        plantDao.markPlantCompleted(pid);

        // 보상 이름 및 설명 조회
        return plantDao.getRewardInfoById(rewardId);
    }
    // 4. 보상 수령 이력
    public List<RewardHistoryDto> getRewardHistory(Long uid) {
        return plantDao.getRewardHistory(uid);
    }
}
