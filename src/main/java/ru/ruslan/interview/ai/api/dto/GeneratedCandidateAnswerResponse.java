package ru.ruslan.interview.ai.api.dto;

public record GeneratedCandidateAnswerResponse(
        String candidateId,
        String answer,
        boolean correct,
        String reaction
) {
}
