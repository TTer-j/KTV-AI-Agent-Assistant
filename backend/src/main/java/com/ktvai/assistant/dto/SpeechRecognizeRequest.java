package com.ktvai.assistant.dto;

import lombok.Data;

@Data
public class SpeechRecognizeRequest {
    private String audio;
    private String format = "webm";
    private Integer sampleRate = 16000;
    private String language = "zh-CN";
}
