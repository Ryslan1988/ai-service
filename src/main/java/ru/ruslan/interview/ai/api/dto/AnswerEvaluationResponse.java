package ru.ruslan.interview.ai.api.dto;

public record AnswerEvaluationResponse(
        boolean correct,
        int score,
        String explanation,
        String feedback
) {
}
