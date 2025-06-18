package com.example.demo.surveyResult.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HistoryMapper {
    @Insert("INSERT INTO History(uid, bug_id) VALUES (#{uid}, #{bugId})")
    void insertHistory(@Param("uid") Long uid, @Param("bugId") Long bugId);
}
