package com.yanfranko.reto.inventory.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "Productos")

@Entity
public class Producto {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column(name = "id_producto")
    private Long productoId;

    // nombre del producto valga la redundancia
    @Column(nullable = false, length = 150)
    private String nombre;

    // Descripción del producto
    @Column(length = 500)
    private String description;

    //precio para el producto
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    // la cantidad que hay en el inventario
    @Column(nullable = false)
    private Integer stock;

    // el estado del producto (para saber si esta disponible) igual lo maneraje con cuardrillas
    @Column(nullable = false)
    private Boolean estado;

    // Fecha de creacion del producto
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    // Fecha de modificacion del producto
    @Column(name = "fecha_modificacion", nullable = false)
    private Instant fechaModificacion;



}
