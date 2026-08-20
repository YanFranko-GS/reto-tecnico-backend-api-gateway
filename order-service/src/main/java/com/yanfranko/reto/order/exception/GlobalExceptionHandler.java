package com.yanfranko.reto.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;

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