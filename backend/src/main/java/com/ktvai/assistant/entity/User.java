package com.ktvai.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String nickname;
    
    private String avatar;
    
    private String preferenceTags;
    
    private Integer age;
    
    private String gender;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}