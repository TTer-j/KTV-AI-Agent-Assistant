package com.ktvai.assistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ktvai.assistant.mapper")
public class KtvAiAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(KtvAiAssistantApplication.class, args);
    }
}