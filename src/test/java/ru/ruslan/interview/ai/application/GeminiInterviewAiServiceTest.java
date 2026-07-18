package ru.ruslan.interview.ai.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.ruslan.interview.ai.api.dto.CandidateProfileRequest;
import ru.ruslan.interview.ai.api.dto.GenerateQuestionRequest;
import ru.ruslan.interview.ai.api.dto.GeneratedCandidateAnswerResponse;
import ru.ruslan.interview.ai.api.dto.GeneratedQuestionResponse;

class GeminiInterviewAiServiceTest {

    private final GeminiInterviewAiService service = new GeminiInterviewAiService(
            null,
            new ObjectMapper()
    );

    @Test
    void acceptsOneQuestionWithFourCandidateAnswersAndSingleCorrectAnswer() {
        var request = request();
        var response = response(List.of(
                answer("one", true),
                answer("two", false),
                answer("three", false),
                answer("four", false)
        ));

        assertDoesNotThrow(() -> service.requireQuestionResponse(response, request));
    }

    @Test
    void rejectsMoreThanOneCorrectAnswer() {
        var response = response(List.of(
                answer("one", true),
                answer("two", true),
                answer("three", false),
                answer("four", false)
        ));

        assertThrows(
                AiResponseException.class,
                () -> service.requireQuestionResponse(response, request())
        );
    }

    @Test
    void rejectsAnswerForUnknownCandidate() {
        var response = response(List.of(
                answer("one", true),
                answer("two", false),
                answer("three", false),
                answer("unknown", false)
        ));

        assertThrows(
                AiResponseException.class,
                () -> service.requireQuestionResponse(response, request())
        );
    }

    @Test
    void rejectsQuestionAlreadyPresentInHistory() {
        assertThrows(
                AiResponseException.class,
                () -> service.requireQuestionResponse(
                        response(List.of(
                                answer("one", true),
                                answer("two", false),
                                answer("three", false),
                                answer("four", false)
                        )),
                        request(List.of("Как работает volatile?"), List.of())
                )
        );
    }

    @Test
    void rejectsRepeatedCandidateAnswers() {
        var duplicate = new GeneratedCandidateAnswerResponse(
                "two",
                "Одинаковая формулировка ответа кандидата",
                false,
                "unsure"
        );
        var anotherDuplicate = new GeneratedCandidateAnswerResponse(
                "three",
                "Одинаковая формулировка ответа кандидата",
                false,
                "thinking"
        );

        assertThrows(
                AiResponseException.class,
                () -> service.requireQuestionResponse(
                        response(List.of(
                                answer("one", true),
                                duplicate,
                                anotherDuplicate,
                                answer("four", false)
                        )),
                        request()
                )
        );
    }

    @Test
    void rejectsAnswerAlreadyPresentInHistory() {
        assertThrows(
                AiResponseException.class,
                () -> service.requireQuestionResponse(
                        response(List.of(
                                answer("one", true),
                                answer("two", false),
                                answer("three", false),
                                answer("four", false)
                        )),
                        request(
                                List.of(),
                                List.of("volatile устанавливает мониторную блокировку")
                        )
                )
        );
    }

    private GenerateQuestionRequest request() {
        return request(List.of(), List.of());
    }

    private GenerateQuestionRequest request(
            List<String> previousQuestions,
            List<String> previousAnswers
    ) {
        return new GenerateQuestionRequest(
                "Java",
                "MIDDLE+",
                6,
                "test-seed",
                previousQuestions,
                previousAnswers,
                List.of(
                        candidate("one"),
                        candidate("two"),
                        candidate("three"),
                        candidate("four")
                )
        );
    }

    private CandidateProfileRequest candidate(String id) {
        return new CandidateProfileRequest(id, id, "MIDDLE+", "спокойный");
    }

    private GeneratedQuestionResponse response(
            List<GeneratedCandidateAnswerResponse> answers
    ) {
        return new GeneratedQuestionResponse(
                "Как работает volatile?",
                "Гарантирует видимость изменений между потоками.",
                answers,
                "Проверяется Java Memory Model.",
                6
        );
    }

    private GeneratedCandidateAnswerResponse answer(
            String candidateId,
            boolean correct
    ) {
        var answerText = switch (candidateId) {
            case "two" -> "volatile устанавливает мониторную блокировку";
            case "three" -> "изменения видны только в локальном кеше потока";
            case "four" -> "составной инкремент становится полностью атомарным";
            default -> "Гарантируется видимость записи между потоками";
        };
        return new GeneratedCandidateAnswerResponse(
                candidateId,
                answerText,
                correct,
                "confident"
        );
    }
}
