package ro.eduplatform.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ro.eduplatform.domain.Submission;
import ro.eduplatform.domain.TestEntity;
import ro.eduplatform.domain.User;
import ro.eduplatform.dto.SubmissionDtos;
import ro.eduplatform.dto.TestDtos;
import ro.eduplatform.service.AnalyticsService;
import ro.eduplatform.service.SubmissionService;
import ro.eduplatform.service.TestService;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final TestService testService;
    private final SubmissionService submissionService;
    private final AnalyticsService analyticsService;
    private final CurrentUser currentUser;

    @GetMapping("/tests")
    public List<TestDtos.TestSummaryDto> listTests() {
        User u = currentUser.get();
        if (u.getGradeLevel() == null) {
            throw new ApiException(400, "Elev fara clasa setata");
        }
        analyticsService.record(u, "TEST_LIST_VIEW", null, "grade=" + u.getGradeLevel());
        return testService.listForGrade(u.getGradeLevel());
    }

    @GetMapping("/tests/{id}")
    public TestDtos.StudentTestDetailDto getTest(@PathVariable Long id) {
        User u = currentUser.get();
        TestEntity t = testService.getOrThrow(id);
        if (u.getGradeLevel() != null && !u.getGradeLevel().equals(t.getGradeLevel())) {
            throw new ApiException(403, "Acest test nu este pentru clasa ta");
        }
        analyticsService.record(u, "TEST_VIEW", id, null);
        return testService.toStudentDetail(t);
    }

    @PostMapping("/submissions")
    public SubmissionDtos.SubmissionResult submit(@Valid @RequestBody SubmissionDtos.SubmitTestRequest req) {
        User u = currentUser.get();
        return submissionService.submit(u, req);
    }

    @GetMapping("/submissions")
    public List<SubmissionDtos.SubmissionSummary> mySubmissions() {
        User u = currentUser.get();
        return submissionService.findByStudent(u).stream()
                .map(submissionService::toSummary)
                .toList();
    }

    @GetMapping("/submissions/{id}")
    public SubmissionDtos.SubmissionResult getSubmission(@PathVariable Long id) {
        User u = currentUser.get();
        Submission s = submissionService.getOrThrow(id);
        if (!s.getStudent().getId().equals(u.getId())) {
            throw new ApiException(403, "Nu ai acces la acest rezultat");
        }
        return submissionService.toResult(s);
    }
}
