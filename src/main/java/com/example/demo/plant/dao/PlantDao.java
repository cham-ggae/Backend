package com.example.demo.plant.dao;

import com.example.demo.plant.dto.PlantStatusResponseDto;
import com.example.demo.plant.dto.RewardHistoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlantDao {

    // 새 식물 레코드 생성
    void insertPlant(@Param("fid") Long fid, @Param("kid") int kid);
    // 가장 최근 생성된 식물 상태 조회 (레벨, 경험치 등 // 내림차순으로 하나 선택)
    PlantStatusResponseDto selectLatestPlantByFid(@Param("fid") Long fid);
    // 해당 가족의 구성원 수 확인
    int selectFamilyMemberCount(@Param("fid") Long fid);
    // 아직 완료되지 않은 식물이 존재하는지 확인
    boolean hasUncompletedPlant(@Param("fid") Long fid);
    // 식물을 완료 상태로 변경
    void markPlantCompleted(@Param("pid") Long pid);
    // 보상 수령 로그 기록
    void insertRewardLog(@Param("uid") Long uid, @Param("fid") Long fid, @Param("pid") Long pid, @Param("rewardId") int rewardId);
    //유저의 보상 수령 이력 조회
    List<RewardHistoryDto> getRewardHistory(@Param("uid") Long uid);
    //입력받은 식물 타입(flower/tree)에 해당
    int getPlantKindId(@Param("type") String plantType);
    // 가족 스페이스 확인
    Long getUserFid(@Param("uid") Long uid);
    // 가장 마지막 식물 id 확인
    Long getLatestPlantId(@Param("fid") Long fid);
    // 완료 된 식물
    boolean isPlantCompleted(@Param("pid") Long pid);

}