package com.ai_practice.github.smart_task_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI taskManagerOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Smart Task Manager API")
                .version("v1")
                .description("REST APIs for managing tasks"));
    }
}
