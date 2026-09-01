package com.yanfranko.reto.order.exception;

public class InventoryServiceUnavailableException extends RuntimeException {
    public  InventoryServiceUnavailableException(String message) {
        super ("No se pudo conectar con el servicio de inventario: " + message);
    }

}
