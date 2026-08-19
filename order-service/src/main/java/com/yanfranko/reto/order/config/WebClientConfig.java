package com.yanfranko.reto.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // configuracion para la comunicacion sincrona
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
