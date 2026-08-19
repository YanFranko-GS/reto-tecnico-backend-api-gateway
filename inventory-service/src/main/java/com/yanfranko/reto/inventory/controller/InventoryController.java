package com.yanfranko.reto.inventory.controller;

import com.yanfranko.reto.inventory.dto.DisponibilidadResponseDto;
import com.yanfranko.reto.inventory.dto.ProductoResponseDto;
import com.yanfranko.reto.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventario/producto")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productoId}")
    public ResponseEntity<ProductoResponseDto> obtenerProductoPorId(@PathVariable Long productoId) {
        return ResponseEntity.ok(
                inventoryService.obtenerProductoPorId(productoId)
        );
    }

    // compuba la disponibilidad del producto para una cantidad determinada
    @GetMapping("/{productoId}/disponibilidad")
    public ResponseEntity<DisponibilidadResponseDto> verifiDisponibilidad(
            @PathVariable Long productoId,
            @RequestParam Integer cantidadSolicitada
    ) {
        return ResponseEntity.ok(
                inventoryService.verificarDisponibilidad(
                        productoId,
                        cantidadSolicitada
                )
        );
    }
}
