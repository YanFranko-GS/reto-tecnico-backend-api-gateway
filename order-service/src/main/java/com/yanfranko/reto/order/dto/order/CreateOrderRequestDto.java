package com.yanfranko.reto.order.dto.order;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequestDto(

        @NotNull(message = "El productId es obligatorio")
        @Positive(message = "El productId debe ser mayor que cero")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer quantity

) {
}
