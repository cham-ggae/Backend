package com.example.demo.plant.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NutrientDao {
    int getNutrientStock(@Param("fid") Long fid);
}
