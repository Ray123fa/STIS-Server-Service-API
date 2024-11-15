package com.polstat.penyediaanserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("STIS Server Service API")
                        .version("1.0")
                        .description("This API provides access to the server services at STIS, intended for authorized users only."));
    }

    @Bean
    public OpenApiCustomizer filterOpenApiCustomizer() {
        return openApi -> openApi.getPaths().keySet().removeIf(path ->
                !path.startsWith("/register") && !path.startsWith("/login") && !path.startsWith("/api"));
    }
}
