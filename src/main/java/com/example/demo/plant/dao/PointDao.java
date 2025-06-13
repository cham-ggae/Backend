package com.example.demo.plant.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface PointDao {
    // 사용자가 오늘 특정 활동 했는지
    boolean checkActivityExists(@Param("uid") Long uid, @Param("type") String type);
    //해당 사용자의 가족 공간 ID 조회
    Long getFamilyIdByUid(@Param("uid") Long uid);

    // 활동 수행 내역
    void insertActivity(Map<String, Object> activity);

    // 현재 경험치 조회
    int getCurrentExperience(@Param("pid") Long pid);
    // 식물 id 로 현재 식물 레벨 조회
    int getPlantLevel(@Param("pid") Long pid);
    // 경험치 업데이트
    void updateExperience(@Param("pid") Long pid, @Param("exp") int experience);
    // 해당 식물 레벨 1개 올리도록 설정
    void levelUp(@Param("pid") Long pid);

    // 가족 공간의 구성원 수 조회
    int getFamilyMemberCount(@Param("fid") Long fid);
    // 가족 공간의 식물 조회
    Long getPlantIdByFid(@Param("fid") Long fid);

    // 물주기 확인 백업 api
    // 오늘 날짜 기준으로, 해당 가족(fid)에 속한 유저 중 'water' 활동을 한 유저
    List<Long> getTodayWateredUids(@Param("fid") Long fid, @Param("date") Date date);

    String getUserName(@Param("uid") Long uid);
    String getUserProfile(@Param("uid") Long uid);

    // 오늘 물 준 가족 구성원 수
    int countWateredMembersToday(@Param("fid") Long fid, @Param("date") Date date);
    // 영양제 +1 처리
    void incrementNutrient(@Param("fid") Long fid);
}
