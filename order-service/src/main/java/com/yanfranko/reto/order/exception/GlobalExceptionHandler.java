package com.yanfranko.reto.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // añadimos eso para que el tracerId no bote null
    //se aladio el x-trace-id
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
            OrderNotFoundException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                exception.getMessage(),
                exchange

        );
    }

    //manejo de errores de comunicacion
    @ExceptionHandler(InventoryServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleInventoryUnavailable(
            InventoryServiceUnavailableException exception,
            ServerWebExchange exchange
    ){
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "INVENTORY_SERVICE_UNAVAILABLE",
                exception.getMessage(),
                exchange
        );
    }





    @ExceptionHandler(InvalidOrderTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderTransition(
            InvalidOrderTransitionException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "INVALID_ORDER_TRANSITION",
                exception.getMessage(),
                exchange
        );
    }

    //agregamos esto nuevo para el 400 INVALID_REQUEST
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            WebExchangeBindException exception,
            ServerWebExchange exchange
    ) {

        String traceId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Trace-Id");

        String message = exception.getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_REQUEST",
                message,
                traceId
        );

        exchange.getResponse()
                .getHeaders()
                .set("X-Trace-Id", traceId);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
    ////////////////

    @ExceptionHandler(StockInsufficientException.class)
    public ResponseEntity<ErrorResponse> handleStockInsufficient(
            StockInsufficientException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "STOCK_INSUFFICIENT",
                exception.getMessage(),
                exchange
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception,
            ServerWebExchange exchange
    ) {

        exception.printStackTrace();

        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = "Se produjo un error interno en el servidor";
        }

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                message,
                exchange
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            ServerWebExchange exchange
    ) {

        String traceId = exchange.getRequest()
                .getHeaders()
                .getFirst(TRACE_ID_HEADER);

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                traceId
        );

        exchange.getResponse()
                .getHeaders()
                .set(TRACE_ID_HEADER, traceId);

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }
}