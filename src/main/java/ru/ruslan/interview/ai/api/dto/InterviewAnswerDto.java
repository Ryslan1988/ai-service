package ru.ruslan.interview.ai.api.dto;

import jakarta.validation.constraints.NotBlank;

public record InterviewAnswerDto(
        @NotBlank String question,
        @NotBlank String answer,
        boolean correct
) {
}
