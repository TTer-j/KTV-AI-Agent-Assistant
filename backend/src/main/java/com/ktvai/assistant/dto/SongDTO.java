package com.ktvai.assistant.dto;

import lombok.Data;

@Data
public class SongDTO {
    
    private Long id;
    
    private String songName;
    
    private String singer;
    
    private String album;
    
    private String genre;
    
    private String sceneTags;

    private String externalId;
    
    private String coverUrl;
    
    private String audioUrl;
    
    private Integer popularity;
    
    private Integer duration;
}
