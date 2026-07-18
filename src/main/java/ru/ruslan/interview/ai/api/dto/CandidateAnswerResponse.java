package ru.ruslan.interview.ai.api.dto;

public record CandidateAnswerResponse(
        String answer,
        boolean expectedCorrect,
        String reaction
) {
}
