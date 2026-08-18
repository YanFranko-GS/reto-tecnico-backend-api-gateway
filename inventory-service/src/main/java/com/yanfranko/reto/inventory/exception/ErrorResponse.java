package com.yanfranko.reto.inventory.exception;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String messege,
        String traceId
) {
}
