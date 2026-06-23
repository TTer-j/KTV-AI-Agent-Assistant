package com.ktvai.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_log")
public class ChatLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String sessionId;
    
    private Long userId;
    
    private String userInput;
    
    private String aiResponse;
    
    private String intentType;
    
    private String preferenceTags;
    
    private LocalDateTime createdAt;
}