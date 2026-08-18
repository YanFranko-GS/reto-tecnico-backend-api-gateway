package com.yanfranko.reto.order.service;

import com.yanfranko.reto.order.dto.OrderStatusHistory.OrderHistoryResponseDto;
import com.yanfranko.reto.order.dto.order.CreateOrderRequestDto;
import com.yanfranko.reto.order.dto.order.OrderResponseDto;
import com.yanfranko.reto.order.entity.Order;
import com.yanfranko.reto.order.entity.OrderStatusHistory;
import com.yanfranko.reto.order.entity.enums.OrderStatus;
import com.yanfranko.reto.order.exception.InvalidOrderTransitionException;
import com.yanfranko.reto.order.exception.OrderNotFoundException;
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

    public OrderService(
            OrderRepository orderRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    // Crea un pedido con el estado inicial PENDING
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto request) {

        Instant now = Instant.now();

        Order order = Order.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .status(OrderStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .previousStatus(null)
                .newStatus(OrderStatus.PENDING)
                .changedAt(now)
                .reason("El pedido ha sido creado")
                .build();

        orderStatusHistoryRepository.save(history);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    // Consulta un pedido por su ID
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return OrderResponseDto.fromEntity(order);
    }

    // Consulta el historial de un pedido
    @Transactional(readOnly = true)
    public List<OrderHistoryResponseDto> getOrderHistory(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return orderStatusHistoryRepository.findByOrderOrderId(order.getOrderId())
                .stream()
                .map(OrderHistoryResponseDto::fromEntity)
                .toList();
    }

    // Cancela un pedido cuando el estado actual lo permite
    @Transactional
    public OrderResponseDto cancelarOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderTransitionException(
                    order.getStatus(),
                    OrderStatus.CANCELLED
            );
        }

        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {

            throw new InvalidOrderTransitionException(
                    order.getStatus(),
                    OrderStatus.CANCELLED
            );
        }

        OrderStatus previousStatus = order.getStatus();
        Instant now = Instant.now();

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(now);

        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .previousStatus(previousStatus)
                .newStatus(OrderStatus.CANCELLED)
                .changedAt(now)
                .reason("El pedido ha sido cancelado")
                .build();

        orderStatusHistoryRepository.save(history);

        return OrderResponseDto.fromEntity(savedOrder);
    }
}