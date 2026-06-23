package com.ktvai.assistant.dto;

import lombok.Data;

@Data
public class ChatQuery {
    
    private String sessionId;
    
    private String userInput;
    
    private Long userId;
}