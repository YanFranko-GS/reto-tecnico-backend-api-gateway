package com.yanfranko.reto.order.exception;

public class OrderNotFoundException extends RuntimeException {

    public  OrderNotFoundException(Long orderId) {
        super ("No se encontro el pedido con el ID: " + orderId);
    }
}
