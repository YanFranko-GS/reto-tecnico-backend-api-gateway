package com.yanfranko.reto.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InventoryClient {

    private final WebClient webClient;

    public InventoryClient(
            WebClient.Builder webClientBuilder,
            @Value("${inventory.service.url}") String inventoryServiceUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    public Mono<AvailabilityResponse> checkAvailability(
            Long productoId,
            Integer cantidad
    ) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/inventario/producto/{productoId}/disponibilidad")
                        .queryParam("cantidadSolicitada", cantidad)
                        .build(productoId)
                )
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> Mono.error(
                                new RuntimeException(
                                        "Se produjo un error al consultar el producto en Inventory Service"
                                )
                        )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(
                                new RuntimeException(
                                        "Inventory Service no está disponible"
                                )
                        )
                )
                .bodyToMono(AvailabilityResponse.class);
    }

    public record AvailabilityResponse(
            Long productoId,
            Integer cantidadSolicitada,
            Integer stockDisponible,
            Boolean disponible
    ) {
    }
}