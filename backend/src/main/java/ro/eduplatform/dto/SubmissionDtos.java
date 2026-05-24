package ro.eduplatform.dto;

import jakarta.validation.constraints.NotNull;
import ro.eduplatform.domain.Subject;

import java.time.Instant;
import java.util.List;

public class SubmissionDtos {

    public record AnswerInput(
            @NotNull Long questionId,
            String answer
    ) {}

    public record SubmitTestRequest(
            @NotNull Long testId,
            @NotNull List<AnswerInput> answers,
            Integer timeSpentSeconds
    ) {}

    /**
     * Feedback per intrebare: dezvaluie raspunsul corect dupa submit.
     */
    public record AnswerFeedback(
            Long questionId,
            String prompt,
            String givenAnswer,
            String correctAnswer,
            Boolean correct,
            Integer pointsEarned,
            Integer pointsMax,
            String explanation
    ) {}

    public record SubmissionResult(
            Long submissionId,
            Long testId,
            String testTitle,
            Integer score,
            Integer maxScore,
            Double grade,
            Instant submittedAt,
            List<AnswerFeedback> feedback
    ) {}

    public record SubmissionSummary(
            Long submissionId,
            Long testId,
            String testTitle,
            Subject subject,
            Integer gradeLevel,
            Integer score,
            Integer maxScore,
            Double grade,
            Instant submittedAt,
            String studentName,
            String studentUsername
    ) {}
}
