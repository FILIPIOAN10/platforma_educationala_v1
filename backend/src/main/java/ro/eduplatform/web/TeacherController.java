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
import ro.eduplatform.service.SubmissionService;
import ro.eduplatform.service.TestService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class TeacherController {

    private final TestService testService;
    private final SubmissionService submissionService;
    private final CurrentUser currentUser;

    @GetMapping("/tests")
    public List<TestDtos.TestSummaryDto> myTests() {
        User u = currentUser.get();
        return testService.listForTeacher(u);
    }

    @GetMapping("/tests/all")
    public List<TestDtos.TestSummaryDto> allTests() {
        return testService.listAll();
    }

    @PostMapping("/tests")
    public TestDtos.AdminTestDetailDto create(@Valid @RequestBody TestDtos.CreateTestRequest req) {
        User u = currentUser.get();
        TestEntity test = testService.createTest(req, u);
        return testService.toAdminDetail(test);
    }

    @GetMapping("/tests/{id}")
    public TestDtos.AdminTestDetailDto getTest(@PathVariable Long id) {
        return testService.toAdminDetail(testService.getOrThrow(id));
    }

    @PutMapping("/tests/{id}")
    public TestDtos.AdminTestDetailDto update(@PathVariable Long id, @Valid @RequestBody TestDtos.UpdateTestRequest req) {
        User u = currentUser.get();
        return testService.toAdminDetail(testService.updateTest(id, req, u));
    }

    @DeleteMapping("/tests/{id}")
    public void delete(@PathVariable Long id) {
        User u = currentUser.get();
        testService.deleteTest(id, u);
    }

    /**
     * Toate submisiile pentru testele profesorului curent.
     */
    @GetMapping("/results")
    public List<SubmissionDtos.SubmissionSummary> resultsForMyTests() {
        User u = currentUser.get();
        List<TestEntity> myTests = testService.listForTeacher(u).stream()
                .map(s -> testService.getOrThrow(s.id()))
                .toList();
        return myTests.stream()
                .flatMap(t -> submissionService.findByTest(t).stream())
                .map(submissionService::toSummary)
                .toList();
    }

    @GetMapping("/tests/{id}/results")
    public List<SubmissionDtos.SubmissionSummary> resultsForTest(@PathVariable Long id) {
        TestEntity t = testService.getOrThrow(id);
        return submissionService.findByTest(t).stream()
                .map(submissionService::toSummary)
                .toList();
    }

    @GetMapping("/results/{id}")
    public SubmissionDtos.SubmissionResult resultDetail(@PathVariable Long id) {
        Submission s = submissionService.getOrThrow(id);
        return submissionService.toResult(s);
    }

    /**
     * Statistici agregate pe testele profesorului.
     */
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        User u = currentUser.get();
        List<TestEntity> myTests = testService.listForTeacher(u).stream()
                .map(s -> testService.getOrThrow(s.id()))
                .toList();

        Map<String, Object> resp = new HashMap<>();
        resp.put("totalTests", myTests.size());

        List<Submission> all = myTests.stream()
                .flatMap(t -> submissionService.findByTest(t).stream())
                .toList();
        resp.put("totalSubmissions", all.size());

        double avg = all.stream().mapToDouble(Submission::getGrade).average().orElse(0.0);
        resp.put("averageGrade", Math.round(avg * 100.0) / 100.0);

        Map<String, Double> bySubject = new LinkedHashMap<>();
        Map<String, Long> countBySubject = new LinkedHashMap<>();
        for (Submission s : all) {
            String key = s.getTest().getSubject().name();
            bySubject.merge(key, s.getGrade(), Double::sum);
            countBySubject.merge(key, 1L, Long::sum);
        }
        Map<String, Double> avgBySubject = new LinkedHashMap<>();
        bySubject.forEach((k, v) -> avgBySubject.put(k, Math.round((v / countBySubject.get(k)) * 100.0) / 100.0));
        resp.put("averageBySubject", avgBySubject);

        Map<String, Long> byTest = new LinkedHashMap<>();
        Map<String, Double> avgByTest = new LinkedHashMap<>();
        Map<String, Double> sumByTest = new LinkedHashMap<>();
        for (Submission s : all) {
            String key = s.getTest().getTitle();
            byTest.merge(key, 1L, Long::sum);
            sumByTest.merge(key, s.getGrade(), Double::sum);
        }
        sumByTest.forEach((k, v) -> avgByTest.put(k, Math.round((v / byTest.get(k)) * 100.0) / 100.0));
        resp.put("submissionsByTest", byTest);
        resp.put("averageByTest", avgByTest);

        return resp;
    }
}
