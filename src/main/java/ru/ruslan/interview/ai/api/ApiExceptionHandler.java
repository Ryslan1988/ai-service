package ru.ruslan.interview.ai.api;

import java.time.Instant;
import java.util.Map;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.ruslan.interview.ai.application.AiResponseException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null
                                ? "Некорректное значение"
                                : error.getDefaultMessage(),
                        (first, ignored) -> first
                ));

        return response(HttpStatus.BAD_REQUEST, "Ошибка валидации", errors);
    }

    @ExceptionHandler(TransientAiException.class)
    ResponseEntity<Map<String, Object>> transientAi(TransientAiException exception) {
        log.error("Временная ошибка при обращении к AI-провайдеру", exception);
        return response(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AI-провайдер временно недоступен",
                Map.of()
        );
    }

    @ExceptionHandler({NonTransientAiException.class, AiResponseException.class})
    ResponseEntity<Map<String, Object>> aiError(RuntimeException exception) {
        log.error("Ошибка при обращении к AI-провайдеру", exception);
        return response(
                HttpStatus.BAD_GATEWAY,
                "Не удалось обработать ответ AI-провайдера",
                Map.of()
        );
    }

    private ResponseEntity<Map<String, Object>> response(
            HttpStatus status,
            String message,
            Map<String, ?> details
    ) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now(),
                "status", status.value(),
                "error", message,
                "details", details
        ));
    }
}
