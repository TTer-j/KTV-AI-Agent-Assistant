package com.ktvai.assistant.agent;

import com.ktvai.assistant.entity.ChatLog;
import com.ktvai.assistant.mapper.ChatLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DialogueContext {
    
    private final ChatLogMapper chatLogMapper;
    
    private static final int MAX_CONTEXT_SIZE = 10;
    
    private final Map<String, List<ChatLog>> contextCache = new ConcurrentHashMap<>();
    private final Map<String, String> preferencesCache = new ConcurrentHashMap<>();
    
    public String getOrCreateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }
    
    public List<ChatLog> getHistory(String sessionId) {
        return contextCache.getOrDefault(sessionId, new ArrayList<>());
    }
    
    public void saveContext(String sessionId, String userInput, String aiResponse, String intentType) {
        List<ChatLog> history = contextCache.computeIfAbsent(sessionId, k -> new ArrayList<>());
        
        ChatLog chatLog = new ChatLog();
        chatLog.setSessionId(sessionId);
        chatLog.setUserInput(userInput);
        chatLog.setAiResponse(aiResponse);
        chatLog.setIntentType(intentType);
        chatLog.setCreatedAt(LocalDateTime.now());
        
        history.add(0, chatLog);
        
        if (history.size() > MAX_CONTEXT_SIZE) {
            history = history.subList(0, MAX_CONTEXT_SIZE);
        }
        
        contextCache.put(sessionId, history);
        chatLogMapper.insert(chatLog);
    }
    
    public String buildContextPrompt(String sessionId) {
        List<ChatLog> history = getHistory(sessionId);
        if (history.isEmpty()) {
            return "";
        }
        
        StringBuilder prompt = new StringBuilder("对话历史：\n");
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatLog log = history.get(i);
            prompt.append("用户: ").append(log.getUserInput()).append("\n");
            prompt.append("助手: ").append(log.getAiResponse()).append("\n");
        }
        return prompt.toString();
    }
    
    public void clearContext(String sessionId) {
        contextCache.remove(sessionId);
    }
    
    public void updateUserPreferences(String sessionId, Long userId, String tags) {
        preferencesCache.put(sessionId, tags);
    }
    
    public String getUserPreferences(String sessionId) {
        return preferencesCache.getOrDefault(sessionId, "");
    }
}