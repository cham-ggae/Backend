package com.example.demo.plant.dao;

import com.example.demo.plant.dto.PlantResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlantDao {

    // 가장 최근 식물 조회
    PlantResponseDto findLatestPlantByFamilyId(@Param("fid") int fid);

    // 새 식물 생성
    void insertPlant(@Param("fid") int fid, @Param("type") String type);

    // 성장 완료된 식물 여부 확인
    boolean isPlantCompleted(@Param("fid") int fid);

    // 완료 처리
    void completePlant(@Param("pid") int pid);
}