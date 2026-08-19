package com.yanfranko.reto.inventory.dto;

import com.yanfranko.reto.inventory.entity.Producto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductoResponseDto(
        Long productoId,
        String nombre,
        String description,
        BigDecimal precio,
        Integer stock,
        Boolean estado,
        Instant fechaCreacion,
        Instant fechaModificacion
) {

    // Convierte la entidad Producto en su DTO de respuesta
    public static ProductoResponseDto fromEntity(Producto producto) {
        return new ProductoResponseDto(
                producto.getProductoId(),
                producto.getNombre(),
                producto.getDescription(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getEstado(),
                producto.getFechaCreacion(),
                producto.getFechaModificacion()
        );
    }
}