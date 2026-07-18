package ru.ruslan.interview.ai.api.dto;

import jakarta.validation.constraints.NotBlank;

public record EvaluateAnswerRequest(
        @NotBlank String question,
        @NotBlank String expectedAnswer,
        @NotBlank String userAnswer
) {
}
