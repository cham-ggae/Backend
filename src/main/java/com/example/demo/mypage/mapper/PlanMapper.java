package com.example.demo.mypage.mapper;

import com.example.demo.mypage.dto.MyPageResponse;
import com.example.demo.mypage.dto.RecommendHistoryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
/**
 * PlanMapper 클래스입니다.
 */
public interface PlanMapper {
    @Select("SELECT plan_id AS planId, plan_name AS planName, price, discount_price AS discountPrice, benefit, link FROM Plans WHERE plan_id = #{planId}")
    RecommendHistoryData findPlanById(int planId);
}
