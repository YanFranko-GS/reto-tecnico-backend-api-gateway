package com.yanfranko.reto.order.service;

import com.yanfranko.reto.order.client.InventoryClient;
import com.yanfranko.reto.order.dto.order.CreateOrderRequestDto;
import com.yanfranko.reto.order.dto.order.OrderResponseDto;
import com.yanfranko.reto.order.entity.Order;
import com.yanfranko.reto.order.entity.enums.OrderStatus;
import com.yanfranko.reto.order.exception.InvalidOrderTransitionException;
import com.yanfranko.reto.order.exception.OrderNotFoundException;
import com.yanfranko.reto.order.exception.StockInsufficientException;
import com.yanfranko.reto.order.repository.OrderRepository;
import com.yanfranko.reto.order.repository.OrderStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.Long;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequestDto request;
    private InventoryClient.AvailabilityResponse disponibilidad;
    private JwtAuthenticationToken authentication;

    @BeforeEach
    void setUp() {

        request = new CreateOrderRequestDto(
                1L,
                2
        );

        disponibilidad = new InventoryClient.AvailabilityResponse(
                1L,
                2,
                10,
                true
        );

        Jwt jwt = Jwt.withTokenValue("token-prueba")
                .header("alg", "RS256")
                .subject("usuario-prueba")
                .claim("preferred_username", "yanfranko")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        authentication = new JwtAuthenticationToken(jwt);
    }

    @Test
    void deberiaCrearPedidoCuandoHayStock() {

        Order orderGuardado = Order.builder()
                .orderId(1L)
                .productoId(1L)
                .cantidad(2)
                .estado(OrderStatus.CONFIRMED)
                .fechaCreacion(Instant.now())
                .fechaModificacion(Instant.now())
                .build();

        when(inventoryClient.checkAvailability(
                1L,
                2,
                "trace-test-001",
                "token-prueba"
        )).thenReturn(Mono.just(disponibilidad));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(orderGuardado);

        when(orderStatusHistoryRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<OrderResponseDto> result =
                orderService.createOrder(
                        request,
                        "trace-test-001"
                );

        StepVerifier.create(
                        result.contextWrite(
                                ReactiveSecurityContextHolder
                                        .withAuthentication(authentication)
                        )
                )
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(1L, response.orderId());
                    assertEquals(1L, response.productoId());
                    assertEquals(2, response.cantidad());
                    assertEquals(
                            OrderStatus.CONFIRMED,
                            response.estado()
                    );
                })
                .verifyComplete();

        verify(inventoryClient).checkAvailability(
                1L,
                2,
                "trace-test-001",
                "token-prueba"
        );

        verify(orderRepository).save(any(Order.class));
        verify(orderStatusHistoryRepository).save(any());
    }

    @Test
    void deberiaRechazarPedidoCuandoNoHayStock() {

        InventoryClient.AvailabilityResponse sinStock =
                new InventoryClient.AvailabilityResponse(
                        1L,
                        20,
                        10,
                        false
                );

        when(inventoryClient.checkAvailability(
                1L,
                2,
                "trace-test-002",
                "token-prueba"
        )).thenReturn(Mono.just(sinStock));

        Mono<OrderResponseDto> result =
                orderService.createOrder(
                        request,
                        "trace-test-002"
                );

        StepVerifier.create(
                        result.contextWrite(
                                ReactiveSecurityContextHolder
                                        .withAuthentication(authentication)
                        )
                )
                .expectError(StockInsufficientException.class)
                .verify();

        verify(inventoryClient).checkAvailability(
                1L,
                2,
                "trace-test-002",
                "token-prueba"
        );

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(orderStatusHistoryRepository, never())
                .save(any());
    }

    @Test
    void deberiaObtenerPedidoPorId() {

        Order order = Order.builder()
                .orderId(1L)
                .productoId(1L)
                .cantidad(2)
                .estado(OrderStatus.CONFIRMED)
                .fechaCreacion(Instant.now())
                .fechaModificacion(Instant.now())
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponseDto response =
                orderService.getOrderById(
                        1L

                );

        assertNotNull(response);
        assertEquals(1L, response.orderId());
        assertEquals(OrderStatus.CONFIRMED, response.estado());

        verify(orderRepository).findById(1L);
    }

    @Test
    void deberiaLanzarExcepcionSiPedidoNoExiste() {

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(
                        99L


                )
        );

        verify(orderRepository).findById(99L);
    }

    @Test
    void noDeberiaCancelarPedidoYaCancelado() {

        Order order = Order.builder()
                .orderId(1L)
                .productoId(1L)
                .cantidad(2)
                .estado(OrderStatus.CANCELLED)
                .fechaCreacion(Instant.now())
                .fechaModificacion(Instant.now())
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                InvalidOrderTransitionException.class,
                () -> orderService.cancelarOrder(
                        1L,
                        "trace-test-003"

                )
        );

        verify(orderRepository).findById(1L);

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(orderStatusHistoryRepository, never())
                .save(any());
    }
}