package com.example.demo.chatbot.dao;

import com.example.demo.chatbot.dto.Chatting;
import com.example.demo.chatbot.dto.RagSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface ChatbotDao {
    int insertChatting(Chatting chatting)throws SQLException;
    List<Chatting> selectChatting(@Param("sessionId") String sessionId);
    List<RagSource> getRagSource();
}
