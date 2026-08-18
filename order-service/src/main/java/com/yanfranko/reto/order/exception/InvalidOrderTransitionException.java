package com.yanfranko.reto.order.exception;

import com.yanfranko.reto.order.entity.enums.OrderStatus;

public class InvalidOrderTransitionException extends  RuntimeException {

    // este error consiste en que no se puede cambiar de estado que no estan permitidos
    public InvalidOrderTransitionException (
        OrderStatus currentStatus,
        OrderStatus requestedStatus
    ) {
        super(
                "No se permite cambiar de pedido de "
                + currentStatus
                + " a "
                + requestedStatus
        );
    }
}
