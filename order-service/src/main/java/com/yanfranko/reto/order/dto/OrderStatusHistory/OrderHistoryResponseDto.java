package com.yanfranko.reto.order.dto.OrderStatusHistory;

import com.yanfranko.reto.order.entity.OrderStatusHistory;
import com.yanfranko.reto.order.entity.enums.OrderStatus;

import java.time.Instant;

public record OrderHistoryResponseDto(
        Long orderStatusHistoryId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        Instant changedAt,
        String reason
) {
    // Metodo estático para convertir de Entidad a DTO manteniendo tu estilo
    public static OrderHistoryResponseDto fromEntity(OrderStatusHistory history) {
        return new OrderHistoryResponseDto(
                history.getOrderStatusHistoryId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedAt(),
                history.getReason()
        );
    }
}