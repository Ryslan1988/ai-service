package ru.ruslan.interview.ai.api.dto;

import java.util.List;

public record InterviewResultResponse(
        int totalScore,
        String level,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations,
        String summary
) {
}
