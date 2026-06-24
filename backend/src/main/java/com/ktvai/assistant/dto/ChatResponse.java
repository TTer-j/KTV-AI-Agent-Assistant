package com.ktvai.assistant.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    
    private String sessionId;
    
    private String reply;
    
    private List<SongDTO> songs;
    
    private List<PlaylistDTO> playlists;
    
    private boolean clarification;
    
    private String intentType;

    private String aiProvider;

    private boolean aiConfigured;

    private boolean aiUsed;
}
