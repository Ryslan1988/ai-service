package ru.ruslan.interview.ai.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ruslan.interview.ai.api.dto.AnswerEvaluationResponse;
import ru.ruslan.interview.ai.api.dto.CandidateAnswerResponse;
import ru.ruslan.interview.ai.api.dto.EvaluateAnswerRequest;
import ru.ruslan.interview.ai.api.dto.GenerateCandidateAnswerRequest;
import ru.ruslan.interview.ai.api.dto.GenerateQuestionRequest;
import ru.ruslan.interview.ai.api.dto.GeneratedQuestionResponse;
import ru.ruslan.interview.ai.api.dto.InterviewResultRequest;
import ru.ruslan.interview.ai.api.dto.InterviewResultResponse;
import ru.ruslan.interview.ai.application.AiInterviewService;

@RestController
@RequestMapping("/api/v1/ai")
public class AiInterviewController {

    private final AiInterviewService aiInterviewService;

    public AiInterviewController(AiInterviewService aiInterviewService) {
        this.aiInterviewService = aiInterviewService;
    }

    @PostMapping("/questions/generate")
    public GeneratedQuestionResponse generateQuestion(
            @Valid @RequestBody GenerateQuestionRequest request
    ) {
        return aiInterviewService.generateQuestion(request);
    }

    @PostMapping("/candidate-answers/generate")
    public CandidateAnswerResponse generateCandidateAnswer(
            @Valid @RequestBody GenerateCandidateAnswerRequest request
    ) {
        return aiInterviewService.generateCandidateAnswer(request);
    }

    @PostMapping("/answers/evaluate")
    public AnswerEvaluationResponse evaluateAnswer(
            @Valid @RequestBody EvaluateAnswerRequest request
    ) {
        return aiInterviewService.evaluateAnswer(request);
    }

    @PostMapping("/interviews/result")
    public InterviewResultResponse generateInterviewResult(
            @Valid @RequestBody InterviewResultRequest request
    ) {
        return aiInterviewService.generateInterviewResult(request);
    }
}
