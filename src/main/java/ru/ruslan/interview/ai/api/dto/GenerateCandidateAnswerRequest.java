package ru.ruslan.interview.ai.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateCandidateAnswerRequest(
        @NotBlank String question,
        @NotBlank String candidateName,
        @NotBlank String candidateLevel,
        @NotBlank String candidatePersonality,
        boolean correct
) {
}
