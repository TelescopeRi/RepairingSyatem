package com.dorm.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.repair.entity.ChatMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY timestamp DESC LIMIT #{limit}")
    List<ChatMessage> findRecentBySessionId(String sessionId, int limit);
    
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY timestamp DESC LIMIT #{offset}, #{limit}")
    List<ChatMessage> findByOffset(@Param("sessionId") String sessionId, @Param("limit") int limit, @Param("offset") int offset);
    
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY timestamp ASC")
    List<ChatMessage> findAllBySessionId(String sessionId);
    
    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId}")
    int countBySessionId(String sessionId);
}
