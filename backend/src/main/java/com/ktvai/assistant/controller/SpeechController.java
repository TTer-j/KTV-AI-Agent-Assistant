package com.ktvai.assistant.controller;

import com.ktvai.assistant.dto.Result;
import com.ktvai.assistant.dto.SpeechRecognizeRequest;
import com.ktvai.assistant.dto.SpeechRecognizeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/speech")
@Tag(name = "语音接口", description = "语音识别相关接口")
public class SpeechController {

    @PostMapping("/recognize")
    @Operation(summary = "语音识别", description = "将音频数据识别为文字")
    public Result<SpeechRecognizeResponse> recognize(@RequestBody SpeechRecognizeRequest request) {
        try {
            String text = mockSpeechRecognize(request.getAudio());
            SpeechRecognizeResponse response = new SpeechRecognizeResponse();
            response.setText(text);
            response.setConfidence(0.95);
            return Result.success(response);
        } catch (Exception e) {
            log.error("语音识别失败", e);
            return Result.error(500, "语音识别失败：" + e.getMessage());
        }
    }

    private String mockSpeechRecognize(String audioBase64) {
        if (audioBase64 == null || audioBase64.isEmpty()) {
            return "";
        }

        Map<String, String> mockTexts = new HashMap<>();
        mockTexts.put("default", "来点周杰伦的歌");

        int length = Math.min(audioBase64.length(), 100);
        long hash = 0;
        for (int i = 0; i < length; i++) {
            hash = hash * 31 + audioBase64.charAt(i);
        }

        String[] samples = {
            "来点周杰伦的歌",
            "生日派对歌单",
            "来点摇滚",
            "我想听陈奕迅",
            "安静一点的歌",
            "来首情歌"
        };

        int index = (int) (Math.abs(hash) % samples.length);
        return samples[index];
    }
}
