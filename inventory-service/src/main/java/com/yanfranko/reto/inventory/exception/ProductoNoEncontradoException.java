package com.yanfranko.reto.inventory.exception;

public class ProductoNoEncontradoException extends RuntimeException {

    public  ProductoNoEncontradoException(Long productoId) {
        super(" No se encontro el producto con el ID: " + productoId);
    }
}
