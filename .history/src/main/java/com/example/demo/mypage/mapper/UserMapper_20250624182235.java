package com.example.demo.mypage.mapper;

import com.example.demo.mypage.dto.MyPageResponse;
import org.apache.ibatis.annotations.*;

@Mapper
/**
 * UserMapper 클래스입니다.
 */
public interface UserMapper {
    @Select("SELECT name, profile_image, bug_id FROM Users WHERE uid = #{uid}")
    @Results({
            @Result(property = "name", column = "name"),
            @Result(property = "profileImage", column = "profile_image"),
            @Result(property = "bugId", column = "bug_id")
    })
    MyPageResponse.UserInfo findUserInfoById(@Param("uid") Long uid);
}
