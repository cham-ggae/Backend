package com.example.demo.surveyResult.mapper;

import com.example.demo.mypage.dto.RecommendHistoryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MypageMapper {
    @Select("""
            SELECT
                        p.plan_id AS planId,
                        p.plan_name AS planName,
                        p.price AS price,
                        p.discount_price AS discountPrice,
                        p.benefit AS benefit,
                        p.link AS link,
                        h.created_at AS createdAt
                    FROM History h
                    JOIN Bugs b ON h.bug_id = b.bug_id
                    JOIN Plans p ON b.suggest1 = p.plan_id
                    WHERE h.uid = #{userId}
            
                    UNION ALL
            
                    SELECT
                        p.plan_id AS planId,
                        p.plan_name AS planName,
                        p.price AS price,
                        p.discount_price AS discountPrice,
                        p.benefit AS benefit,
                        p.link AS link,
                        h.created_at AS createdAt
                    FROM History h
                    JOIN Bugs b ON h.bug_id = b.bug_id
                    JOIN Plans p ON b.suggest2 = p.plan_id
                    WHERE h.uid = #{userId}
        """)
    List<RecommendHistoryData> findRecommendHistory(Long userId);
}
