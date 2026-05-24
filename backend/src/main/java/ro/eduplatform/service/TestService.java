package ro.eduplatform.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.eduplatform.domain.AnswerOption;
import ro.eduplatform.domain.Question;
import ro.eduplatform.domain.QuestionType;
import ro.eduplatform.domain.TestEntity;
import ro.eduplatform.domain.User;
import ro.eduplatform.dto.TestDtos;
import ro.eduplatform.repository.TestRepository;
import ro.eduplatform.repository.UserRepository;
import ro.eduplatform.web.ApiException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final UserRepository userRepository;

    public List<TestDtos.TestSummaryDto> listAll() {
        return testRepository.findAll().stream().map(this::toSummary).toList();
    }

    public List<TestDtos.TestSummaryDto> listForGrade(int grade) {
        return testRepository.findByGradeLevel(grade).stream().map(this::toSummary).toList();
    }

    public List<TestDtos.TestSummaryDto> listForTeacher(User teacher) {
        return testRepository.findByCreator(teacher).stream().map(this::toSummary).toList();
    }

    public TestEntity getOrThrow(Long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "Test inexistent"));
    }

    @Transactional
    public TestEntity createTest(TestDtos.CreateTestRequest req, User creator) {
        TestEntity test = TestEntity.builder()
                .title(req.title())
                .description(req.description())
                .subject(req.subject())
                .gradeLevel(req.gradeLevel())
                .chapter(req.chapter())
                .difficulty(req.difficulty())
                .creator(creator)
                .build();
        applyQuestions(test, req.questions());
        return testRepository.save(test);
    }

    @Transactional
    public TestEntity updateTest(Long id, TestDtos.UpdateTestRequest req, User actor) {
        TestEntity test = getOrThrow(id);
        // doar admin sau creator
        boolean isAdmin = actor.getRole() != null && actor.getRole().name().equals("ADMIN");
        if (!isAdmin && (test.getCreator() == null || !test.getCreator().getId().equals(actor.getId()))) {
            throw new ApiException(403, "Nu poti edita acest test");
        }
        test.setTitle(req.title());
        test.setDescription(req.description());
        test.setSubject(req.subject());
        test.setGradeLevel(req.gradeLevel());
        test.setChapter(req.chapter());
        test.setDifficulty(req.difficulty());
        test.getQuestions().clear();
        applyQuestions(test, req.questions());
        return testRepository.save(test);
    }

    @Transactional
    public void deleteTest(Long id, User actor) {
        TestEntity test = getOrThrow(id);
        boolean isAdmin = actor.getRole() != null && actor.getRole().name().equals("ADMIN");
        if (!isAdmin && (test.getCreator() == null || !test.getCreator().getId().equals(actor.getId()))) {
            throw new ApiException(403, "Nu poti sterge acest test");
        }
        testRepository.delete(test);
    }

    private void applyQuestions(TestEntity test, List<TestDtos.QuestionAdminDto> qs) {
        int qIdx = 0;
        for (TestDtos.QuestionAdminDto q : qs) {
            if (q.type() == QuestionType.MULTIPLE_CHOICE) {
                if (q.options() == null || q.options().isEmpty()) {
                    throw new ApiException(400, "Intrebarile cu varianta multipla trebuie sa aiba optiuni");
                }
                boolean anyCorrect = q.options().stream().anyMatch(o -> Boolean.TRUE.equals(o.correct()));
                if (!anyCorrect) {
                    throw new ApiException(400, "Cel putin o optiune trebuie marcata ca raspuns corect");
                }
            } else if (q.type() == QuestionType.TRUE_FALSE) {
                if (q.correctAnswerText() == null
                        || !(q.correctAnswerText().equalsIgnoreCase("true") || q.correctAnswerText().equalsIgnoreCase("false"))) {
                    throw new ApiException(400, "Pentru adevarat/fals, raspunsul trebuie sa fie 'true' sau 'false'");
                }
            } else if (q.type() == QuestionType.SHORT_ANSWER) {
                if (q.correctAnswerText() == null || q.correctAnswerText().isBlank()) {
                    throw new ApiException(400, "Intrebarile cu raspuns scurt necesita raspuns corect");
                }
            }

            Question question = Question.builder()
                    .test(test)
                    .type(q.type())
                    .prompt(q.prompt())
                    .imageUrl(q.imageUrl())
                    .correctAnswerText(q.correctAnswerText())
                    .explanation(q.explanation())
                    .points(q.points() == null ? 1 : q.points())
                    .orderIndex(q.orderIndex() == null ? qIdx : q.orderIndex())
                    .options(new ArrayList<>())
                    .build();

            if (q.options() != null) {
                int oIdx = 0;
                for (TestDtos.OptionDto o : q.options()) {
                    AnswerOption opt = AnswerOption.builder()
                            .question(question)
                            .text(o.text())
                            .correct(Boolean.TRUE.equals(o.correct()))
                            .orderIndex(o.orderIndex() == null ? oIdx : o.orderIndex())
                            .build();
                    question.getOptions().add(opt);
                    oIdx++;
                }
            }
            test.getQuestions().add(question);
            qIdx++;
        }
    }

    public TestDtos.TestSummaryDto toSummary(TestEntity t) {
        return new TestDtos.TestSummaryDto(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getSubject(),
                t.getGradeLevel(),
                t.getChapter(),
                t.getDifficulty(),
                t.getQuestions().size(),
                t.getCreator() != null ? t.getCreator().getFullName() : null,
                t.getCreatedAt()
        );
    }

    public TestDtos.StudentTestDetailDto toStudentDetail(TestEntity t) {
        List<TestDtos.QuestionStudentDto> qs = t.getQuestions().stream().map(q ->
                new TestDtos.QuestionStudentDto(
                        q.getId(),
                        q.getType(),
                        q.getPrompt(),
                        q.getImageUrl(),
                        q.getOrderIndex(),
                        q.getPoints(),
                        q.getOptions().stream()
                                .map(o -> new TestDtos.OptionStudentDto(o.getId(), o.getText(), o.getOrderIndex()))
                                .toList()
                )
        ).toList();
        return new TestDtos.StudentTestDetailDto(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getSubject(), t.getGradeLevel(), t.getChapter(), t.getDifficulty(),
                qs
        );
    }

    public TestDtos.AdminTestDetailDto toAdminDetail(TestEntity t) {
        List<TestDtos.QuestionAdminDto> qs = t.getQuestions().stream().map(q ->
                new TestDtos.QuestionAdminDto(
                        q.getId(),
                        q.getType(),
                        q.getPrompt(),
                        q.getImageUrl(),
                        q.getCorrectAnswerText(),
                        q.getExplanation(),
                        q.getOrderIndex(),
                        q.getPoints(),
                        q.getOptions().stream()
                                .map(o -> new TestDtos.OptionDto(o.getId(), o.getText(), o.getCorrect(), o.getOrderIndex()))
                                .toList()
                )
        ).toList();
        return new TestDtos.AdminTestDetailDto(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getSubject(), t.getGradeLevel(), t.getChapter(), t.getDifficulty(),
                t.getCreator() != null ? t.getCreator().getFullName() : null,
                t.getCreatedAt(),
                qs
        );
    }
}
