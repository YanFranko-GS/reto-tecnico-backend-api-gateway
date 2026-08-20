package com.yanfranko.reto.order.controller;

import com.yanfranko.reto.order.dto.OrderStatusHistory.OrderHistoryResponseDto;
import com.yanfranko.reto.order.dto.order.CreateOrderRequestDto;
import com.yanfranko.reto.order.dto.order.OrderResponseDto;
import com.yanfranko.reto.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Registrar un nuevo pedido

    //se aladio el x-trace-id
    @PostMapping
    public Mono<ResponseEntity<OrderResponseDto>> createOrder(
            @Valid @RequestBody CreateOrderRequestDto request,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    ) {

        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        return orderService
                .createOrder(request, traceId)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    // Consultar un pedido por su ID
    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<OrderResponseDto>> getOrderById(
            @PathVariable Long orderId
    ) {
        return Mono.fromCallable(() ->
                        orderService.getOrderById(orderId)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }

    // Consultar el historial de estados de un pedido
    @GetMapping("/{orderId}/history")
    public Mono<ResponseEntity<List<OrderHistoryResponseDto>>> getOrderHistory(
            @PathVariable Long orderId
    ) {
        return Mono.fromCallable(() ->
                        orderService.getOrderHistory(orderId)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }

    // Cancelar un pedido
    @PostMapping("/{orderId}/cancel")
    public Mono<ResponseEntity<OrderResponseDto>> cancelOrder(
            @PathVariable Long orderId
    ) {
        return Mono.fromCallable(() ->
                        orderService.cancelarOrder(orderId)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }
}