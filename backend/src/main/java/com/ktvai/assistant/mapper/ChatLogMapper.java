package com.ktvai.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ktvai.assistant.entity.ChatLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatLogMapper extends BaseMapper<ChatLog> {
    
    @Select("SELECT * FROM chat_log WHERE session_id = #{sessionId} ORDER BY created_at DESC")
    List<ChatLog> findBySessionId(@Param("sessionId") String sessionId);
    
    @Select("SELECT * FROM chat_log WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<ChatLog> findRecentByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
}