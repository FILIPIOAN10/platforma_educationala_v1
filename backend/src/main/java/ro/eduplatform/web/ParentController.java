package ro.eduplatform.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ro.eduplatform.domain.Submission;
import ro.eduplatform.domain.Subject;
import ro.eduplatform.domain.User;
import ro.eduplatform.dto.ParentDtos;
import ro.eduplatform.service.SubmissionService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARENT')")
public class ParentController {

    private final SubmissionService submissionService;
    private final CurrentUser currentUser;

    private static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    @GetMapping("/children")
    public List<ParentDtos.ChildDto> children() {
        User parent = currentUser.get();
        return parent.getChildren().stream().map(this::toChild).toList();
    }

    @GetMapping("/dashboard")
    public List<ParentDtos.ChildProgress> dashboard() {
        User parent = currentUser.get();
        return parent.getChildren().stream()
                .map(this::buildProgress)
                .toList();
    }

    @GetMapping("/children/{childId}/progress")
    public ParentDtos.ChildProgress childProgress(@PathVariable Long childId) {
        User parent = currentUser.get();
        User child = parent.getChildren().stream()
                .filter(c -> c.getId().equals(childId))
                .findFirst()
                .orElseThrow(() -> new ApiException(403, "Acest copil nu este asociat contului tau"));
        return buildProgress(child);
    }

    private ParentDtos.ChildProgress buildProgress(User child) {
        List<Submission> subs = submissionService.findByStudent(child);
        double overall = subs.stream().mapToDouble(Submission::getGrade).average().orElse(0.0);

        Map<Subject, List<Submission>> bySubject = subs.stream()
                .collect(Collectors.groupingBy(s -> s.getTest().getSubject()));
        List<ParentDtos.SubjectAverage> subjectAverages = bySubject.entrySet().stream()
                .map(e -> {
                    double avg = e.getValue().stream().mapToDouble(Submission::getGrade).average().orElse(0.0);
                    return new ParentDtos.SubjectAverage(e.getKey(),
                            Math.round(avg * 100.0) / 100.0, e.getValue().size());
                })
                .sorted(Comparator.comparing(ParentDtos.SubjectAverage::subject))
                .toList();

        List<ParentDtos.RecentResult> recent = subs.stream()
                .limit(10)
                .map(s -> new ParentDtos.RecentResult(
                        s.getId(),
                        s.getTest().getId(),
                        s.getTest().getTitle(),
                        s.getTest().getSubject(),
                        s.getTest().getChapter(),
                        s.getScore(),
                        s.getMaxScore(),
                        s.getGrade(),
                        s.getSubmittedAt()
                ))
                .toList();

        // Evolutie: media zilnica
        Map<String, List<Double>> byDay = subs.stream().collect(Collectors.groupingBy(
                s -> DAY_FMT.format(s.getSubmittedAt()),
                LinkedHashMap::new,
                Collectors.mapping(Submission::getGrade, Collectors.toList())
        ));
        Map<String, Double> evolution = new LinkedHashMap<>();
        byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    double avg = e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    evolution.put(e.getKey(), Math.round(avg * 100.0) / 100.0);
                });

        return new ParentDtos.ChildProgress(
                toChild(child),
                Math.round(overall * 100.0) / 100.0,
                subs.size(),
                subjectAverages,
                recent,
                evolution
        );
    }

    private ParentDtos.ChildDto toChild(User c) {
        return new ParentDtos.ChildDto(c.getId(), c.getUsername(), c.getFullName(), c.getGradeLevel());
    }
}
