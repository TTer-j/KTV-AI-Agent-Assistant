package com.ktvai.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("song")
public class Song {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String songName;
    
    private String singer;
    
    private String album;
    
    private String genre;
    
    private String sceneTags;
    
    private String externalId;
    
    private String audioUrl;
    
    private String coverUrl;
    
    private Integer popularity;
    
    private Integer duration;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}