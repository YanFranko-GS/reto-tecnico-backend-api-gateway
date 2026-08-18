package com.yanfranko.reto.order.controller;

import com.yanfranko.reto.order.dto.OrderStatusHistory.OrderHistoryResponseDto;
import com.yanfranko.reto.order.dto.order.CreateOrderRequestDto;
import com.yanfranko.reto.order.dto.order.OrderResponseDto;
import com.yanfranko.reto.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Registrar un nuevo pedido
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto request
    ) {
        OrderResponseDto response = orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Consultar un pedido por su ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                orderService.getOrderById(orderId)
        );
    }

    // Consultar el historial de estados de un pedido
    @GetMapping("/{orderId}/history")
    public ResponseEntity<List<OrderHistoryResponseDto>> getOrderHistory(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                orderService.getOrderHistory(orderId)
        );
    }

    // Cancelar un pedido
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                orderService.cancelarOrder(orderId)
        );
    }
}