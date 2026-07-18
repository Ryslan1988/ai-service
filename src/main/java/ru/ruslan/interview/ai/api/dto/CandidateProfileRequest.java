package ru.ruslan.interview.ai.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CandidateProfileRequest(
        @NotBlank String id,
        @NotBlank String name,
        @NotBlank String level,
        @NotBlank String personality
) {
}
