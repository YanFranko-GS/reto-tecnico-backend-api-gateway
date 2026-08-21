package com.yanfranko.reto.order.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    //configuarion para el swuagger
    @Bean
    public OpenAPI orderServiceOpenAPI() {

        SecurityScheme bearerToken = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("API para gestionar pedidos e historial de estados")
                        .version("1.0.0"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        bearerToken
                                )
                );
    }
}