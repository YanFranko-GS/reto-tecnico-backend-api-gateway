package com.yanfranko.reto.order.dto.order;

import com.yanfranko.reto.order.entity.enums.OrderStatus;

import java.time.Instant;

public record OrderResponse(

        Long orderId,
        Long productId,
        Integer quantity,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt

) {
}