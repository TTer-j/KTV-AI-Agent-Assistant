package com.ktvai.assistant.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlaylistDTO {
    
    private Long id;
    
    private String playlistName;
    
    private String description;
    
    private List<SongDTO> songs;
    
    private Integer playCount;
}