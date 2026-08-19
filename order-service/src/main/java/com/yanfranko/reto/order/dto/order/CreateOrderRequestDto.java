package com.yanfranko.reto.order.dto.order;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequestDto(

        @NotNull(message = "El productoId es obligatorio")
        @Positive(message = "El productoId debe ser mayor que cero")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad

) {
}
