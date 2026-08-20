package com.yanfranko.reto.order.service;

import com.yanfranko.reto.order.client.InventoryClient;
import com.yanfranko.reto.order.dto.OrderStatusHistory.OrderHistoryResponseDto;
import com.yanfranko.reto.order.dto.order.CreateOrderRequestDto;
import com.yanfranko.reto.order.dto.order.OrderResponseDto;
import com.yanfranko.reto.order.entity.Order;
import com.yanfranko.reto.order.entity.OrderStatusHistory;
import com.yanfranko.reto.order.entity.enums.OrderStatus;
import com.yanfranko.reto.order.exception.InvalidOrderTransitionException;
import com.yanfranko.reto.order.exception.OrderNotFoundException;
import com.yanfranko.reto.order.exception.StockInsufficientException;
import com.yanfranko.reto.order.repository.OrderRepository;
import com.yanfranko.reto.order.repository.OrderStatusHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final InventoryClient inventoryClient;

    public OrderService(
            OrderRepository orderRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            InventoryClient inventoryClient
    ) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.inventoryClient = inventoryClient;
    }

    // Crea un pedido verificando previamente la disponibilidad del producto
    //se aladio el x-trace-id
    //añadimos la propagacion del token
    public Mono<OrderResponseDto> createOrder(
            CreateOrderRequestDto request,
            String traceId
    ) {

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(authentication ->
                        authentication.getToken().getTokenValue()
                )
                .flatMap(accessToken ->

                        inventoryClient
                                .checkAvailability(
                                        request.productoId(),
                                        request.cantidad(),
                                        traceId,
                                        accessToken
                                )
                                .flatMap(availability -> {

                                    log.info(
                                            "[traceId={}] Disponibilidad recibida para producto {}: {}",
                                            traceId,
                                            request.productoId(),
                                            availability.disponible()
                                    );

                                    if (!Boolean.TRUE.equals(
                                            availability.disponible()
                                    )) {
                                        return Mono.error(
                                                new StockInsufficientException(
                                                        request.productoId()
                                                )
                                        );
                                    }

                                    return Mono.fromCallable(() -> {

                                        Instant now = Instant.now();

                                        Order order = Order.builder()
                                                .productoId(request.productoId())
                                                .cantidad(request.cantidad())
                                                .estado(OrderStatus.CONFIRMED)
                                                .fechaCreacion(now)
                                                .fechaModificacion(now)
                                                .build();

                                        Order savedOrder =
                                                orderRepository.save(order);

                                        OrderStatusHistory history =
                                                OrderStatusHistory.builder()
                                                        .order(savedOrder)
                                                        .previousEstado(null)
                                                        .nuevoEstado(
                                                                OrderStatus.CONFIRMED
                                                        )
                                                        .fechaModificacion(now)
                                                        .razonCambio(
                                                                "El pedido ha sido confirmado por disponibilidad de stock"
                                                        )
                                                        .build();

                                        orderStatusHistoryRepository.save(history);

                                        log.info(
                                                "[traceId={}] Pedido {} confirmado correctamente",
                                                traceId,
                                                savedOrder.getOrderId()
                                        );

                                        return OrderResponseDto
                                                .fromEntity(savedOrder);

                                    }).subscribeOn(
                                            Schedulers.boundedElastic()
                                    );
                                })
                );
    }


    // Consulta un pedido por su ID
    public OrderResponseDto getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return OrderResponseDto.fromEntity(order);
    }

    // Consulta el historial de un pedido
    public List<OrderHistoryResponseDto> getOrderHistory(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return orderStatusHistoryRepository
                .findByOrderOrderId(order.getOrderId())
                .stream()
                .map(OrderHistoryResponseDto::fromEntity)
                .toList();
    }

    // Cancela un pedido cuando el estado actual lo permite
    public OrderResponseDto cancelarOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getEstado() == OrderStatus.CANCELLED) {
            throw new InvalidOrderTransitionException(
                    order.getEstado(),
                    OrderStatus.CANCELLED
            );
        }

        if (order.getEstado() != OrderStatus.PENDING
                && order.getEstado() != OrderStatus.CONFIRMED) {

            throw new InvalidOrderTransitionException(
                    order.getEstado(),
                    OrderStatus.CANCELLED
            );
        }

        OrderStatus previousStatus = order.getEstado();
        Instant now = Instant.now();

        order.setEstado(OrderStatus.CANCELLED);
        order.setFechaModificacion(now);

        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .previousEstado(previousStatus)
                .nuevoEstado(OrderStatus.CANCELLED)
                .fechaModificacion(now)
                .razonCambio("El pedido ha sido cancelado")
                .build();

        orderStatusHistoryRepository.save(history);

        return OrderResponseDto.fromEntity(savedOrder);
    }
}