package com.yanfranko.reto.order.entity;

import com.yanfranko.reto.order.entity.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // esto lo estoy poniendo para saber la cantidad que se va a pedir
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    // eh creado un enum donde contendra todas mis listas de opciones
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private OrderStatus estado;

    // para cuando se creo -- Se vera a mas detalle en el postman
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    // y esto es para la ultima modificacion igual se vera en el postman con mas a detalle
    @Column(name = "fecha_modificacion", nullable = false)
    private Instant fechaModificacion;

    // Este es el identificador del producto osea lo que se va a solicitar
    @Column(name = "producto_id", nullable = false)
    private Long productoId;
}