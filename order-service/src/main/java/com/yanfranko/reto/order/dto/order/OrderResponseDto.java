package com.yanfranko.reto.order.dto.order;

import com.yanfranko.reto.order.entity.Order;
import com.yanfranko.reto.order.entity.enums.OrderStatus;

import java.time.Instant;

public record OrderResponseDto(
        Long orderId,
        Long productoId,
        Integer cantidad,
        OrderStatus estado,
        Instant fechaCreacion,
        Instant fechaModificacion
) {
    // Metodo estático opcional para convertir de Entidad a DTO rápidamente
    public static OrderResponseDto fromEntity(Order order) {
        return new OrderResponseDto(
                order.getOrderId(),
                order.getProductoId(),
                order.getCantidad(),
                order.getEstado(),
                order.getFechaCreacion(),
                order.getFechaModificacion()
        );
    }
}