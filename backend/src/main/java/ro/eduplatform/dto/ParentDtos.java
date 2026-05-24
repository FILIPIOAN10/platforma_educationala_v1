package ro.eduplatform.dto;

import ro.eduplatform.domain.Subject;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ParentDtos {

    public record ChildDto(
            Long id,
            String username,
            String fullName,
            Integer gradeLevel
    ) {}

    public record SubjectAverage(
            Subject subject,
            Double average,
            Integer testsTaken
    ) {}

    public record ChildProgress(
            ChildDto child,
            Double overallAverage,
            Integer testsTaken,
            List<SubjectAverage> bySubject,
            List<RecentResult> recent,
            Map<String, Double> evolution
    ) {}

    public record RecentResult(
            Long submissionId,
            Long testId,
            String testTitle,
            Subject subject,
            String chapter,
            Integer score,
            Integer maxScore,
            Double grade,
            Instant submittedAt
    ) {}
}
