package ro.eduplatform.dto;

import jakarta.validation.constraints.*;
import ro.eduplatform.domain.Difficulty;
import ro.eduplatform.domain.QuestionType;
import ro.eduplatform.domain.Subject;

import java.time.Instant;
import java.util.List;

public class TestDtos {

    public record OptionDto(
            Long id,
            @NotBlank String text,
            Boolean correct,
            Integer orderIndex
    ) {}

    /**
     * Folosit cand profesor/admin creeaza/editeaza intrebari (include corectitudine).
     */
    public record QuestionAdminDto(
            Long id,
            @NotNull QuestionType type,
            @NotBlank String prompt,
            String imageUrl,
            String correctAnswerText,
            String explanation,
            Integer orderIndex,
            @Min(1) Integer points,
            List<OptionDto> options
    ) {}

    /**
     * Folosit cand elevul deschide testul: NU contine flag-ul `correct`.
     */
    public record QuestionStudentDto(
            Long id,
            QuestionType type,
            String prompt,
            String imageUrl,
            Integer orderIndex,
            Integer points,
            List<OptionStudentDto> options
    ) {}

    public record OptionStudentDto(
            Long id,
            String text,
            Integer orderIndex
    ) {}

    public record TestSummaryDto(
            Long id,
            String title,
            String description,
            Subject subject,
            Integer gradeLevel,
            String chapter,
            Difficulty difficulty,
            Integer numberOfQuestions,
            String creatorName,
            Instant createdAt
    ) {}

    /**
     * Test detaliat pentru elev (fara raspunsuri corecte).
     */
    public record StudentTestDetailDto(
            Long id,
            String title,
            String description,
            Subject subject,
            Integer gradeLevel,
            String chapter,
            Difficulty difficulty,
            List<QuestionStudentDto> questions
    ) {}

    /**
     * Test detaliat pentru profesor/admin (cu raspunsuri corecte).
     */
    public record AdminTestDetailDto(
            Long id,
            String title,
            String description,
            Subject subject,
            Integer gradeLevel,
            String chapter,
            Difficulty difficulty,
            String creatorName,
            Instant createdAt,
            List<QuestionAdminDto> questions
    ) {}

    public record CreateTestRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 1000) String description,
            @NotNull Subject subject,
            @NotNull @Min(5) @Max(8) Integer gradeLevel,
            @Size(max = 120) String chapter,
            Difficulty difficulty,
            @NotNull @Size(min = 1) List<QuestionAdminDto> questions
    ) {}

    public record UpdateTestRequest(
            @NotBlank String title,
            String description,
            @NotNull Subject subject,
            @NotNull @Min(5) @Max(8) Integer gradeLevel,
            String chapter,
            Difficulty difficulty,
            @NotNull List<QuestionAdminDto> questions
    ) {}
}
