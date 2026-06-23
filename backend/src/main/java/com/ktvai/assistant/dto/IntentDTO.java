package com.ktvai.assistant.dto;

import lombok.Data;

import java.util.List;

@Data
public class IntentDTO {
    
    private String type;
    
    private String songName;
    
    private String singer;
    
    private String scene;
    
    private String genre;
    
    private String clarificationQuestion;
    
    private List<String> keywords;
    
    private boolean needClarify;
}