package com.yanfranko.reto.order.service;


import com.yanfranko.reto.order.dto.OrderStatusHistory.OrderHistoryResponseDto;
import com.yanfranko.reto.order.dto.order.CreateOrderRequestDto;
import com.yanfranko.reto.order.dto.order.OrderResponseDto;
import com.yanfranko.reto.order.entity.Order;
import com.yanfranko.reto.order.entity.OrderStatusHistory;
import com.yanfranko.reto.order.entity.enums.OrderStatus;
import com.yanfranko.reto.order.repository.OrderRepository;
import com.yanfranko.reto.order.repository.OrderStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderService(OrderRepository orderRepository, OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }


    // creamos un pedido con el estado por defecto pendiente
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto request) {
        Order order = Order.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .previousStatus(null)
                .newStatus(OrderStatus.PENDING)
                .changedAt(Instant.now())
                .reason("El pedido a sido creado")
                .build();

        orderStatusHistoryRepository.save(history);

        return OrderResponseDto.fromEntity(savedOrder);
    }


    // para consultar un pedido por su ID
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("pedido no fue encontrado"));

        return OrderResponseDto.fromEntity(order);
    }


    // para consultar el historial de un pedido
    @Transactional
    public List<OrderHistoryResponseDto> getOrderHistory(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("pedido no fue encontrado")
                );

        return orderStatusHistoryRepository.findByOrderOrderId(order.getOrderId())
                .stream()
                .map(OrderHistoryResponseDto::fromEntity)
                .toList();
    }


    // para cancelar un pedido
    @Transactional
    public OrderResponseDto cancelarOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("pedido no fue encontrado")
                );
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("El pedido ya esta cancelado");
        }

        if (order.getStatus() == OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("El pedido no puede ser cancelado");
        }

        OrderStatus previousStatus = order.getStatus();

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());

        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .previousStatus(previousStatus)
                .newStatus(OrderStatus.CANCELLED)
                .changedAt(Instant.now())
                .reason("El pedido a sido cancelado")
                .build();

        orderStatusHistoryRepository.save(history);

        return OrderResponseDto.fromEntity(savedOrder);
    }
}