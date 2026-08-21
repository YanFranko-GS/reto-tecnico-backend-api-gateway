package com.yanfranko.reto.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InventoryClient {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final WebClient webClient;

    /**
     * Cliente HTTP para consultar la disponibilidad de productos
     * en el Inventory Service antes de confirmar un pedido.
     *
     * Propaga el token JWT y el identificador de trazabilidad (X-Trace-Id)
     * para conservar la autenticación y el seguimiento entre modulos.
     */
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
            Integer cantidad,
            String traceId,
            String accessToken
    ) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/inventario/producto/{productoId}/disponibilidad")
                        .queryParam("cantidadSolicitada", cantidad)
                        .build(productoId)
                )
                .header(TRACE_ID_HEADER, traceId)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + accessToken
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