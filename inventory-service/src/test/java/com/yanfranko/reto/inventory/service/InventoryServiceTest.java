package com.yanfranko.reto.inventory.service;

import com.yanfranko.reto.inventory.dto.DisponibilidadResponseDto;
import com.yanfranko.reto.inventory.dto.ProductoResponseDto;
import com.yanfranko.reto.inventory.entity.Producto;
import com.yanfranko.reto.inventory.exception.ProductoNoEncontradoException;
import com.yanfranko.reto.inventory.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();

        producto.setProductoId(1L);
        producto.setNombre("Producto de prueba");
        producto.setDescription("Producto para pruebas");
        producto.setPrecio(new BigDecimal("25.50"));
        producto.setStock(10);
        producto.setEstado(true);
        producto.setFechaCreacion(Instant.now());
        producto.setFechaModificacion(Instant.now());
    }

    @Test
    void deberiaObtenerProductoPorId() {

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        ProductoResponseDto response =
                inventoryService.obtenerProductoPorId(
                        1L,
                        "test-trace-001"
                );

        assertNotNull(response);
        assertEquals(1L, response.productoId());
        assertEquals("Producto de prueba", response.nombre());

        verify(productoRepository).findById(1L);
    }

    @Test
    void deberiaLanzarExcepcionSiProductoNoExiste() {

        when(productoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductoNoEncontradoException.class,
                () -> inventoryService.obtenerProductoPorId(
                        99L,
                        "test-trace-002"
                )
        );

        verify(productoRepository).findById(99L);
    }

    @Test
    void deberiaIndicarStockDisponible() {

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        DisponibilidadResponseDto response =
                inventoryService.verificarDisponibilidad(
                        1L,
                        2,
                        "test-trace-003"
                );

        assertNotNull(response);
        assertTrue(response.disponible());
        assertEquals(10, response.stockDisponible());
        assertEquals(2, response.cantidadSolicitada());

        verify(productoRepository).findById(1L);
    }

    @Test
    void deberiaIndicarStockInsuficiente() {

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        DisponibilidadResponseDto response =
                inventoryService.verificarDisponibilidad(
                        1L,
                        20,
                        "test-trace-004"
                );

        assertNotNull(response);
        assertFalse(response.disponible());
        assertEquals(10, response.stockDisponible());
        assertEquals(20, response.cantidadSolicitada());

        verify(productoRepository).findById(1L);
    }
}