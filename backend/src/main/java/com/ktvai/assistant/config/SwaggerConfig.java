package com.ktvai.assistant.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KTV AI Assistant API")
                        .version("1.0.0")
                        .description("KTV智能点歌助手 - AI Native全栈应用接口文档"));
    }
}