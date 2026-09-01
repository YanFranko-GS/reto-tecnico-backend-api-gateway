package com.yanfranko.reto.order.exception;

public class StockInsufficientException extends RuntimeException {

    public StockInsufficientException(Long productId) {
        super("Stock insuficiente para el producto con id: " + productId);
    }
}


