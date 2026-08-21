package com.yanfranko.reto.inventory.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {


    // se crea un funcion mas que todo para registrar los logs de los metodos de service
    //desde Iniciando y finalizando para medir el tiempo de demoro en milisegundo
    @Around("execution(* com.yanfranko.reto.inventory.service..*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint)
            throws  Throwable {

        String methodName = joinPoint.getSignature().toShortString();

        long start = System.currentTimeMillis();

        log.info("Iniciando {}", methodName);

        try {
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - start;

            log.info(
                    "Finalizando {} - duración={} ms"
                    , methodName,
                    duration
            );
            return  result;
        }

        catch (Exception exception) {
            long duration = System.currentTimeMillis() - start;

            log.error(
                    "Error en {} - duración={} ms - mensaje={}",
                    methodName,
                    duration,
                    exception.getMessage()
            );

            throw exception;
        }


    }
}