package ru.ruslan.interview.ai.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record GenerateQuestionRequest(
        @NotBlank String technology,
        @NotBlank String level,
        @Min(1) @Max(10) int difficulty,
        @NotBlank String variationSeed,
        @NotNull @Size(max = 60) List<@NotBlank String> previousQuestions,
        @NotNull @Size(max = 120) List<@NotBlank String> previousAnswers,
        @NotNull @Size(min = 4, max = 4)
        List<@NotNull @Valid CandidateProfileRequest> candidates
) {
}
