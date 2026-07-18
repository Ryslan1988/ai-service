package ru.ruslan.interview.ai.application;

import ru.ruslan.interview.ai.api.dto.AnswerEvaluationResponse;
import ru.ruslan.interview.ai.api.dto.CandidateAnswerResponse;
import ru.ruslan.interview.ai.api.dto.EvaluateAnswerRequest;
import ru.ruslan.interview.ai.api.dto.GenerateCandidateAnswerRequest;
import ru.ruslan.interview.ai.api.dto.GenerateQuestionRequest;
import ru.ruslan.interview.ai.api.dto.GeneratedQuestionResponse;
import ru.ruslan.interview.ai.api.dto.InterviewResultRequest;
import ru.ruslan.interview.ai.api.dto.InterviewResultResponse;

public interface AiInterviewService {

    GeneratedQuestionResponse generateQuestion(GenerateQuestionRequest request);

    CandidateAnswerResponse generateCandidateAnswer(
            GenerateCandidateAnswerRequest request
    );

    AnswerEvaluationResponse evaluateAnswer(EvaluateAnswerRequest request);

    InterviewResultResponse generateInterviewResult(
            InterviewResultRequest request
    );
}
