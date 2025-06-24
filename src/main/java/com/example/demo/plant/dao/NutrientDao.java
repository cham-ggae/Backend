package com.example.demo.plant.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NutrientDao {
    Integer getNutrientStock(@Param("fid") Long fid);
    Long getFamilyIdByUid(@Param("uid") Long uid);
    void decrementNutrientStock(@Param("fid") Long fid);// 추가
}