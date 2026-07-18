package ru.ruslan.interview.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import ru.ruslan.interview.ai.api.dto.AnswerEvaluationResponse;
import ru.ruslan.interview.ai.api.dto.CandidateAnswerResponse;
import ru.ruslan.interview.ai.api.dto.EvaluateAnswerRequest;
import ru.ruslan.interview.ai.api.dto.GenerateCandidateAnswerRequest;
import ru.ruslan.interview.ai.api.dto.GenerateQuestionRequest;
import ru.ruslan.interview.ai.api.dto.GeneratedQuestionResponse;
import ru.ruslan.interview.ai.api.dto.InterviewResultRequest;
import ru.ruslan.interview.ai.api.dto.InterviewResultResponse;

@Service
public class GeminiInterviewAiService implements AiInterviewService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public GeminiInterviewAiService(
            ChatClient interviewChatClient,
            ObjectMapper objectMapper
    ) {
        this.chatClient = interviewChatClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public GeneratedQuestionResponse generateQuestion(
            GenerateQuestionRequest request
    ) {
        var previousQuestions = request.previousQuestions().isEmpty()
                ? "Отсутствуют"
                : String.join("\n- ", request.previousQuestions());

        var candidatesJson = writeJson(
                request.candidates(),
                "Не удалось сериализовать кандидатов"
        );

        var result = chatClient.prompt()
                .user(user -> user.text("""
                                Сгенерируй один технический вопрос и четыре ответа
                                виртуальных кандидатов для игры-собеседования.

                                Технология: {technology}
                                Уровень кандидата: {level}
                                Сложность от 1 до 10: {difficulty}

                                Ранее заданные вопросы:
                                {previousQuestions}

                                Кандидаты в JSON:
                                {candidates}

                                Не повторяй предыдущие вопросы. Верни ровно четыре
                                элемента в answers — по одному для каждого кандидата.
                                candidateId копируй из входного JSON без изменений.

                                Ровно один ответ должен иметь correct=true и быть
                                технически правильным. Остальные три ответа должны
                                содержать разные правдоподобные, но неочевидные ошибки.
                                Учитывай уровень и характер кандидата, делай реплики
                                естественными и не сообщай в тексте, правильный ли ответ.

                                correctAnswer — эталонный технический ответ. Правильная
                                реплика кандидата должна совпадать с ним по смыслу.
                                reaction — короткая реакция персонажа на английском.
                                """)
                        .param("technology", request.technology())
                        .param("level", request.level())
                        .param("difficulty", request.difficulty())
                        .param("previousQuestions", previousQuestions)
                        .param("candidates", candidatesJson))
                .call()
                .entity(GeneratedQuestionResponse.class);

        return requireQuestionResponse(result, request);
    }

    @Override
    public CandidateAnswerResponse generateCandidateAnswer(
            GenerateCandidateAnswerRequest request
    ) {
        var requirement = request.correct()
                ? "Ответ должен быть технически правильным."
                : "Ответ должен содержать одну неочевидную техническую ошибку.";

        var result = chatClient.prompt()
                .user(user -> user.text("""
                                Ответь на технический вопрос от лица виртуального кандидата.

                                Имя: {candidateName}
                                Уровень: {candidateLevel}
                                Характер: {candidatePersonality}

                                Вопрос:
                                {question}

                                Требование:
                                {requirement}

                                Ответ должен выглядеть естественно.
                                Не говори прямо, является ли ответ правильным.
                                expectedCorrect должно быть равно {expectedCorrect}.
                                reaction — короткое название подходящей реакции персонажа.
                                """)
                        .param("candidateName", request.candidateName())
                        .param("candidateLevel", request.candidateLevel())
                        .param("candidatePersonality", request.candidatePersonality())
                        .param("question", request.question())
                        .param("requirement", requirement)
                        .param("expectedCorrect", request.correct()))
                .call()
                .entity(CandidateAnswerResponse.class);

        return requireResponse(result);
    }

    @Override
    public AnswerEvaluationResponse evaluateAnswer(
            EvaluateAnswerRequest request
    ) {
        var result = chatClient.prompt()
                .user(user -> user.text("""
                                Проверь ответ пользователя на технический вопрос.

                                Вопрос:
                                {question}

                                Эталонный ответ:
                                {expectedAnswer}

                                Ответ пользователя:
                                {userAnswer}

                                Учитывай смысл, а не точное совпадение текста.
                                score должен быть от 0 до 100.
                                correct должен быть true, если score не меньше 70.
                                feedback должен содержать практический совет.
                                """)
                        .param("question", request.question())
                        .param("expectedAnswer", request.expectedAnswer())
                        .param("userAnswer", request.userAnswer()))
                .call()
                .entity(AnswerEvaluationResponse.class);

        return requireResponse(result);
    }

    @Override
    public InterviewResultResponse generateInterviewResult(
            InterviewResultRequest request
    ) {
        var answersJson = writeJson(
                request.answers(),
                "Не удалось сериализовать ответы интервью"
        );

        var result = chatClient.prompt()
                .user(user -> user.text("""
                                Проведи итоговый анализ технического интервью.

                                Позиция: {position}
                                Заявленный уровень: {declaredLevel}

                                Ответы в JSON:
                                {answers}

                                Определи:
                                - итоговую оценку от 0 до 100;
                                - фактический уровень;
                                - сильные стороны;
                                - слабые стороны;
                                - рекомендации;
                                - краткий итог.

                                Не оценивай личность кандидата.
                                Оценивай только технические ответы.
                                """)
                        .param("position", request.position())
                        .param("declaredLevel", request.declaredLevel())
                        .param("answers", answersJson))
                .call()
                .entity(InterviewResultResponse.class);

        return requireResponse(result);
    }

    private String writeJson(Object value, String errorMessage) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new AiResponseException(
                    errorMessage,
                    exception
            );
        }
    }

    GeneratedQuestionResponse requireQuestionResponse(
            GeneratedQuestionResponse response,
            GenerateQuestionRequest request
    ) {
        requireResponse(response);
        if (isBlank(response.question())
                || isBlank(response.correctAnswer())
                || isBlank(response.explanation())
                || response.difficulty() < 1
                || response.difficulty() > 10
                || response.answers() == null
                || response.answers().size() != 4) {
            throw new AiResponseException("AI-провайдер вернул некорректный вопрос");
        }
        if (response.answers().stream().anyMatch(java.util.Objects::isNull)) {
            throw new AiResponseException("AI-провайдер вернул пустой ответ кандидата");
        }

        var requestedIds = request.candidates().stream()
                .map(candidate -> candidate.id())
                .collect(java.util.stream.Collectors.toSet());
        var responseIds = response.answers().stream()
                .map(answer -> answer.candidateId())
                .collect(java.util.stream.Collectors.toSet());
        var correctAnswers = response.answers().stream()
                .filter(answer -> answer.correct())
                .count();
        var invalidAnswer = response.answers().stream().anyMatch(answer ->
                isBlank(answer.candidateId())
                        || isBlank(answer.answer())
                        || isBlank(answer.reaction())
        );

        if (!requestedIds.equals(responseIds)
                || responseIds.size() != 4
                || correctAnswers != 1
                || invalidAnswer) {
            throw new AiResponseException("AI-провайдер вернул некорректные ответы кандидатов");
        }
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private <T> T requireResponse(T response) {
        if (response == null) {
            throw new AiResponseException("AI-провайдер вернул пустой ответ");
        }
        return response;
    }
}
