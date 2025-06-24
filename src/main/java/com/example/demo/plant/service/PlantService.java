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

        // ✅ 현재 식물 레벨 조회
        int level = pointService.getPlantLevel(pid);

        // ✅ 레벨 5 미만이면 보상 수령 불가
        if (level < 5) {
            throw new PlantNotCompletedException("레벨 5가 되지 않아 보상을 수령할 수 없습니다.");
        }

        // ✅ 이미 보상 수령했는지 확인
        if (plantDao.hasAlreadyClaimedReward(uid, pid)) {
            throw new RewardAlreadyClaimedException("이미 보상을 수령한 식물입니다.");
        }

        // ✅ 랜덤 보상 ID 선택
        int rewardId = calculateRewardId(fid, pid);

        // ✅ 보상 수령 기록
        plantDao.insertRewardLog(uid, fid, pid, rewardId);

        // ✅ 이 시점에서 식물을 완료 처리
        plantDao.markPlantCompleted(pid);

        // ✅ 보상 정보 반환
        return plantDao.getRewardInfoById(rewardId);
    }

    public List<RewardHistoryDto> getRewardHistory(Long uid) {
        return plantDao.getRewardHistory(uid);
    }

    public List<RewardHistoryDto> getRewardHistoryByFamily(Long uid) {
        Long fid = plantDao.getUserFid(uid);   // 가족 ID로 변환
        return plantDao.getRewardHistoryByFamily(fid); // 가족 전체 보상 조회
    }

    public void markRewardAsUsed(Long uid, Long rewardLogId) {
        Long userFid = plantDao.getUserFid(uid);
        Long rewardFid = plantDao.getRewardFidByRewardLogId(rewardLogId);

        plantDao.updateRewardLogUsed(rewardLogId);
    }


}
