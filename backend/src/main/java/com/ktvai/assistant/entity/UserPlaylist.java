package com.ktvai.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_playlist")
public class UserPlaylist {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String playlistName;
    
    private String description;
    
    private String songIds;
    
    private Integer playCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}