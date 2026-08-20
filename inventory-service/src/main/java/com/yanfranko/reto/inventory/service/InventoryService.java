package com.yanfranko.reto.inventory.service;

import com.yanfranko.reto.inventory.dto.DisponibilidadResponseDto;
import com.yanfranko.reto.inventory.dto.ProductoResponseDto;
import com.yanfranko.reto.inventory.entity.Producto;
import com.yanfranko.reto.inventory.exception.ProductoNoEncontradoException;
import com.yanfranko.reto.inventory.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryService.class);

    private final ProductoRepository productoRepository;

    public InventoryService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // consulta el producto por su ID
    @Transactional(readOnly = true)
    public ProductoResponseDto obtenerProductoPorId(
            Long productoId,
            String traceId
    ) {

        log.info(
                "[traceId={}] Consultando producto con id={}",
                traceId,
                productoId
        );

        Producto productoEncontrado = productoRepository.findById(productoId)
                .orElseThrow(() ->
                        new ProductoNoEncontradoException(productoId));

        log.info(
                "[traceId={}] Producto {} encontrado correctamente",
                traceId,
                productoId
        );

        return ProductoResponseDto.fromEntity(productoEncontrado);
    }

    // verifica si hay stock disponible del producto
    @Transactional(readOnly = true)
    public DisponibilidadResponseDto verificarDisponibilidad(
            Long productoId,
            Integer cantidadSolicitada,
            String traceId
    ) {

        log.info(
                "[traceId={}] Consultando disponibilidad del producto {} para cantidad {}",
                traceId,
                productoId,
                cantidadSolicitada
        );

        Producto productoEncontrado = productoRepository.findById(productoId)
                .orElseThrow(() ->
                        new ProductoNoEncontradoException(productoId));

        boolean disponible =
                Boolean.TRUE.equals(productoEncontrado.getEstado())
                        && productoEncontrado.getStock() >= cantidadSolicitada;

        log.info(
                "[traceId={}] Producto {} - stock={}, cantidadSolicitada={}, disponible={}",
                traceId,
                productoId,
                productoEncontrado.getStock(),
                cantidadSolicitada,
                disponible
        );

        return new DisponibilidadResponseDto(
                productoEncontrado.getProductoId(),
                cantidadSolicitada,
                productoEncontrado.getStock(),
                disponible
        );
    }
}