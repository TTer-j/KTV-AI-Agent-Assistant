package com.ktvai.assistant.controller;

import com.ktvai.assistant.dto.ChatQuery;
import com.ktvai.assistant.dto.ChatResponse;
import com.ktvai.assistant.dto.Result;
import com.ktvai.assistant.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI对话接口", description = "KTV智能点歌助手对话接口")
public class ChatController {
    
    private final ChatService chatService;
    
    @PostMapping("/chat")
    @Operation(summary = "AI对话点歌", description = "与KTV智能助手对话，支持点歌、场景歌单、闲聊等")
    public Result<ChatResponse> chat(@RequestBody ChatQuery query) {
        ChatResponse response = chatService.chat(query);
        return Result.success(response);
    }
}