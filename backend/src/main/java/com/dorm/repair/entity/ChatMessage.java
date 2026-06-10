package com.dorm.repair.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("session_id")
    private String sessionId;
    
    @TableField("role")
    private String role;
    
    @TableField("content")
    private String content;
    
    @TableField("timestamp")
    private LocalDateTime timestamp;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
