package ro.eduplatform.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.eduplatform.domain.AnswerOption;
import ro.eduplatform.domain.Question;
import ro.eduplatform.domain.QuestionType;
import ro.eduplatform.domain.Submission;
import ro.eduplatform.domain.SubmissionAnswer;
import ro.eduplatform.domain.TestEntity;
import ro.eduplatform.domain.User;
import ro.eduplatform.dto.SubmissionDtos;
import ro.eduplatform.repository.SubmissionRepository;
import ro.eduplatform.repository.TestRepository;
import ro.eduplatform.web.ApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final TestRepository testRepository;
    private final AnalyticsService analyticsService;

    @Transactional
    public SubmissionDtos.SubmissionResult submit(User student, SubmissionDtos.SubmitTestRequest req) {
        TestEntity test = testRepository.findById(req.testId())
                .orElseThrow(() -> new ApiException(404, "Test inexistent"));

        if (student.getGradeLevel() != null && !student.getGradeLevel().equals(test.getGradeLevel())) {
            throw new ApiException(403, "Acest test nu este pentru clasa ta");
        }

        Map<Long, String> answersByQ = new HashMap<>();
        if (req.answers() != null) {
            for (SubmissionDtos.AnswerInput a : req.answers()) {
                answersByQ.put(a.questionId(), a.answer());
            }
        }

        Submission submission = Submission.builder()
                .student(student)
                .test(test)
                .timeSpentSeconds(req.timeSpentSeconds())
                .answers(new ArrayList<>())
                .build();

        int totalPoints = 0;
        int earnedPoints = 0;
        List<SubmissionDtos.AnswerFeedback> feedbacks = new ArrayList<>();

        for (Question q : test.getQuestions()) {
            int qPoints = q.getPoints() == null ? 1 : q.getPoints();
            totalPoints += qPoints;
            String given = answersByQ.get(q.getId());

            EvalResult ev = evaluate(q, given);
            int earned = ev.correct ? qPoints : 0;
            earnedPoints += earned;

            SubmissionAnswer sa = SubmissionAnswer.builder()
                    .submission(submission)
                    .question(q)
                    .givenAnswer(given)
                    .correct(ev.correct)
                    .pointsEarned(earned)
                    .build();
            submission.getAnswers().add(sa);

            feedbacks.add(new SubmissionDtos.AnswerFeedback(
                    q.getId(),
                    q.getPrompt(),
                    ev.givenDisplay,
                    ev.correctDisplay,
                    ev.correct,
                    earned,
                    qPoints,
                    q.getExplanation()
            ));
        }

        double grade = totalPoints == 0 ? 1.0 : 1.0 + 9.0 * earnedPoints / totalPoints;
        grade = Math.round(grade * 100.0) / 100.0;
        submission.setScore(earnedPoints);
        submission.setMaxScore(totalPoints);
        submission.setGrade(grade);

        submissionRepository.save(submission);

        analyticsService.record(student, "TEST_SUBMIT", test.getId(),
                "score=" + earnedPoints + "/" + totalPoints + ";grade=" + grade);

        return new SubmissionDtos.SubmissionResult(
                submission.getId(),
                test.getId(),
                test.getTitle(),
                earnedPoints,
                totalPoints,
                grade,
                submission.getSubmittedAt(),
                feedbacks
        );
    }

    private record EvalResult(boolean correct, String givenDisplay, String correctDisplay) {}

    private EvalResult evaluate(Question q, String given) {
        if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
            AnswerOption correctOpt = q.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getCorrect()))
                    .findFirst().orElse(null);
            String correctText = correctOpt != null ? correctOpt.getText() : "";
            String givenText = "";
            boolean correct = false;
            if (given != null && !given.isBlank()) {
                try {
                    Long chosenId = Long.parseLong(given);
                    AnswerOption chosen = q.getOptions().stream()
                            .filter(o -> o.getId().equals(chosenId))
                            .findFirst().orElse(null);
                    if (chosen != null) {
                        givenText = chosen.getText();
                        correct = Boolean.TRUE.equals(chosen.getCorrect());
                    }
                } catch (NumberFormatException ignored) {
                    // tratat ca nepunctat
                }
            }
            return new EvalResult(correct, givenText, correctText);
        }
        if (q.getType() == QuestionType.TRUE_FALSE) {
            String expected = q.getCorrectAnswerText() == null ? "" : q.getCorrectAnswerText().trim().toLowerCase();
            String givenNorm = given == null ? "" : given.trim().toLowerCase();
            boolean correct = !expected.isEmpty() && expected.equals(givenNorm);
            return new EvalResult(correct,
                    given == null ? "" : given,
                    expected.equals("true") ? "Adevarat" : "Fals");
        }
        // SHORT_ANSWER
        String expected = q.getCorrectAnswerText() == null ? "" : q.getCorrectAnswerText().trim();
        String givenNorm = given == null ? "" : given.trim();
        boolean correct = !expected.isEmpty() && expected.equalsIgnoreCase(givenNorm);
        return new EvalResult(correct, givenNorm, expected);
    }

    public List<Submission> findByStudent(User student) {
        return submissionRepository.findByStudentOrderBySubmittedAtDesc(student);
    }

    public List<Submission> findByTest(TestEntity test) {
        return submissionRepository.findByTestOrderBySubmittedAtDesc(test);
    }

    public List<Submission> findByStudents(List<User> students) {
        if (students == null || students.isEmpty()) return List.of();
        return submissionRepository.findByStudentInOrderBySubmittedAtDesc(students);
    }

    public Submission getOrThrow(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "Rezultat inexistent"));
    }

    public SubmissionDtos.SubmissionResult toResult(Submission s) {
        List<SubmissionDtos.AnswerFeedback> feedback = new ArrayList<>();
        for (SubmissionAnswer a : s.getAnswers()) {
            Question q = a.getQuestion();
            String correctDisplay = computeCorrectDisplay(q);
            String givenDisplay = computeGivenDisplay(q, a.getGivenAnswer());
            feedback.add(new SubmissionDtos.AnswerFeedback(
                    q.getId(),
                    q.getPrompt(),
                    givenDisplay,
                    correctDisplay,
                    a.getCorrect(),
                    a.getPointsEarned(),
                    q.getPoints(),
                    q.getExplanation()
            ));
        }
        return new SubmissionDtos.SubmissionResult(
                s.getId(),
                s.getTest().getId(),
                s.getTest().getTitle(),
                s.getScore(),
                s.getMaxScore(),
                s.getGrade(),
                s.getSubmittedAt(),
                feedback
        );
    }

    private String computeCorrectDisplay(Question q) {
        if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
            return q.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getCorrect()))
                    .findFirst().map(AnswerOption::getText).orElse("");
        }
        if (q.getType() == QuestionType.TRUE_FALSE) {
            String t = q.getCorrectAnswerText() == null ? "" : q.getCorrectAnswerText();
            return t.equalsIgnoreCase("true") ? "Adevarat" : "Fals";
        }
        return q.getCorrectAnswerText() == null ? "" : q.getCorrectAnswerText();
    }

    private String computeGivenDisplay(Question q, String given) {
        if (given == null || given.isBlank()) return "";
        if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
            try {
                Long id = Long.parseLong(given);
                return q.getOptions().stream()
                        .filter(o -> o.getId().equals(id))
                        .findFirst().map(AnswerOption::getText).orElse(given);
            } catch (NumberFormatException ex) {
                return given;
            }
        }
        if (q.getType() == QuestionType.TRUE_FALSE) {
            return given.equalsIgnoreCase("true") ? "Adevarat" : "Fals";
        }
        return given;
    }

    public SubmissionDtos.SubmissionSummary toSummary(Submission s) {
        return new SubmissionDtos.SubmissionSummary(
                s.getId(),
                s.getTest().getId(),
                s.getTest().getTitle(),
                s.getTest().getSubject(),
                s.getTest().getGradeLevel(),
                s.getScore(),
                s.getMaxScore(),
                s.getGrade(),
                s.getSubmittedAt(),
                s.getStudent().getFullName(),
                s.getStudent().getUsername()
        );
    }
}
