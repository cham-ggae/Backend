package com.example.demo.login.dao;

import com.example.demo.login.dto.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

import java.sql.SQLException;

@Mapper
public interface UserDao {
    public User findByEmail(String email) throws SQLException;
    public void joinMembership(@Param("email") String email,@Param("accessToken") String AccessToken,@Param("refreshToken") String RefreshToken, @Param("gender") String gender, @Param("age") String age, @Param("name") String name, @Param("profile_image") String profile_image) throws SQLException;
    public void updateUserInfo(@Param("email") String email, @Param("age") String age, @Param("gender") String gender) throws SQLException;
    public void updateToken(@Param("email") String email,@Param("accessToken") String AccessToken,@Param("refreshToken") String RefreshToken) throws SQLException;

    // 카카오 토큰 관리용 메서드
    public void updateKakaoTokens(@Param("email") String email, @Param("kakaoAccessToken") String kakaoAccessToken, @Param("kakaoRefreshToken") String kakaoRefreshToken) throws SQLException;
    public void clearKakaoTokens(@Param("email") String email) throws SQLException;
}
