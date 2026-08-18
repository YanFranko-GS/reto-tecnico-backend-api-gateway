package com.yanfranko.reto.inventory.dto;


// dto devolvera los datos del inventario
public record DisponibilidadResponseDto (

        Long productoId,
        Integer cantidadSolicitada,
        Integer stockDisponible,
        Boolean disponible

) {}
