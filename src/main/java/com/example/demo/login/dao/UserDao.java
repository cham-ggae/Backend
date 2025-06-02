package com.example.demo.login.dao;

import com.example.demo.login.dto.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;

@Mapper
public interface UserDao {
    public User findByEmail(String email) throws SQLException;
    public void joinMembership(@Param("email") String email,@Param("accessToken") String AccessToken,@Param("refreshToken") String RefreshToken) throws SQLException;
    public void updateToken(@Param("email") String email,@Param("accessToken") String AccessToken,@Param("refreshToken") String RefreshToken) throws SQLException;
}
