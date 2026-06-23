package com.ktvai.assistant.dto;

import lombok.Data;

@Data
public class SpeechRecognizeResponse {
    private String text;
    private Double confidence;
}
