package com.yanfranko.reto.order.config;

import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(-100)
public class TraceIdWebFilter implements WebFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    // esto sirve como filtro global para que toda la trazabilidad tengs un identificador unico
    // mas que todo para poder rastrearlo medinte el gateway
    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {

        String traceId = exchange.getRequest()
                .getHeaders()
                .getFirst(TRACE_ID_HEADER);

        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(TRACE_ID_HEADER, traceId)
                .build();

        ServerWebExchange mutatedExchange = exchange
                .mutate()
                .request(request)
                .build();

        mutatedExchange.getResponse()
                .getHeaders()
                .set(TRACE_ID_HEADER, traceId);

        final String currentTraceId = traceId;

        return chain.filter(mutatedExchange)
                .contextWrite(context ->
                        context.put(TRACE_ID_MDC_KEY, currentTraceId));
    }
}