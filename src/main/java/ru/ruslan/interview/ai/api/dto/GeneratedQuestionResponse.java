package ru.ruslan.interview.ai.api.dto;

import java.util.List;

public record GeneratedQuestionResponse(
        String question,
        String correctAnswer,
        List<GeneratedCandidateAnswerResponse> answers,
        String explanation,
        int difficulty
) {
}
