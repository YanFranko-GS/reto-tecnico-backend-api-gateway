package com.yanfranko.reto.inventory.service;


import com.yanfranko.reto.inventory.dto.DisponibilidadResponseDto;
import com.yanfranko.reto.inventory.dto.ProductoResponseDto;
import com.yanfranko.reto.inventory.entity.Producto;
import com.yanfranko.reto.inventory.exception.ProductoNoEncontradoException;
import com.yanfranko.reto.inventory.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final ProductoRepository productoRepository;

    public InventoryService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    //consulta un productor por su ID
    @Transactional(readOnly = true)
    public ProductoResponseDto obtenerProductoPorId(Long productoId) {

        Producto productoEncontrado = productoRepository.findById(productoId)
                .orElseThrow(() ->
                        new ProductoNoEncontradoException(productoId));

        return ProductoResponseDto.fromEntity(productoEncontrado);
    }



    // comprueba si hay stock suficiente segun la solicitud solicitada
    @Transactional(readOnly = true)
    public DisponibilidadResponseDto verificarDisponibilidad(
            Long productoId,
            Integer cantidadSolicitada
    ) {
        Producto productoEncontrado = productoRepository.findById(productoId)
                .orElseThrow(() ->
                        new ProductoNoEncontradoException(productoId));
        boolean disponible = Boolean.TRUE.equals(productoEncontrado.getEstado())
                && productoEncontrado.getStock() >= cantidadSolicitada;

        return new DisponibilidadResponseDto(
                productoEncontrado.getProductoId(),
                cantidadSolicitada,
                productoEncontrado.getStock(),
                disponible
        );

    }



}
