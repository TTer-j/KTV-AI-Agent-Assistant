package com.ktvai.assistant.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SearchEngine {
    
    public List<String> search(String query) {
        return List.of();
    }
    
    public List<String> search(String query, int topK) {
        return List.of();
    }
    
    public String getTopResult(String query) {
        return "";
    }
}