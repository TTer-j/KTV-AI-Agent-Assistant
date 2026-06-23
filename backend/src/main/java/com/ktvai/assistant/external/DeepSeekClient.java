package com.ktvai.assistant.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktvai.assistant.config.AiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekClient {
    
    private final AiConfig aiConfig;
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(2))
            .readTimeout(Duration.ofSeconds(6))
            .writeTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String chat(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            log.warn("DeepSeek API Key not configured, returning empty response");
            return "";
        }
        
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", aiConfig.getTemperature());
            requestBody.put("max_tokens", 512);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(aiConfig.getBaseUrl() + "/chat/completions")
                    .header("Authorization", "Bearer " + aiConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("DeepSeek API request failed: {}", response.code());
                    return "";
                }
                
                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode choices = root.get("choices");
                
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    return choices.get(0).get("message").get("content").asText();
                }
                
                log.error("DeepSeek API response parsing failed");
                return "";
            }
            
        } catch (Exception e) {
            log.error("DeepSeek API call error", e);
            return "";
        }
    }
    
    public String chat(String userPrompt) {
        return chat(null, userPrompt);
    }

    public boolean isConfigured() {
        String apiKey = aiConfig.getApiKey();
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("your-deepseek-api-key");
    }
}
