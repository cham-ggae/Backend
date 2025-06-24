package com.example.demo.mypage.mapper;

import com.example.demo.mypage.dto.MyPageResponse;
import org.apache.ibatis.annotations.*;

@Mapper
/**
 * UserMapper 클래스입니다.
 */
public interface UserMapper {
    @Select("SELECT name, profile_image, bug_id, survey_date FROM Users WHERE uid = #{uid}")
    @Results({
            @Result(property = "name", column = "name"),
            @Result(property = "profileImage", column = "profile_image"),
            @Result(property = "bugId", column = "bug_id"),
            @Result(property = "surveyDate", column = "survey_date")
    })
    MyPageResponse.UserInfo findUserInfoById(@Param("uid") Long uid);
}
