package com.runfit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${springdoc.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${springdoc.server-description:로컬 서버}")
    private String serverDescription;

    @Bean
    public OpenAPI toTastyApi() {
        return new OpenAPI()
            .info(new Info()
                .title("RunFit API")
                .description("RunFit API 명세서")
                .version("v1"))
            .servers(List.of(new Server().url(serverUrl).description(serverDescription)))
            .components(new Components()
                .addSecuritySchemes("AccessToken",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("AccessToken"));
    }
}
