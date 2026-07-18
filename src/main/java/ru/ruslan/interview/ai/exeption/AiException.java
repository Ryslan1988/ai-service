package ru.ruslan.interview.ai.exeption;

import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.ruslan.interview.ai.application.AiResponseException;

import java.time.Instant;
import java.util.Map;

public class AiException extends RuntimeException {
    @ExceptionHandler({
            NonTransientAiException.class,
            AiResponseException.class
    })
    ResponseEntity<Map<String, Object>> aiError(RuntimeException exception) {
        exception.printStackTrace();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "timestamp", Instant.now(),
                "status", HttpStatus.BAD_GATEWAY.value(),
                "error", "Не удалось обработать ответ AI-провайдера",
                "exception", exception.getClass().getName(),
                "message", exception.getMessage() == null
                        ? ""
                        : exception.getMessage(),
                "cause", exception.getCause() == null
                        ? ""
                        : String.valueOf(exception.getCause().getMessage())
        ));
    }
}
