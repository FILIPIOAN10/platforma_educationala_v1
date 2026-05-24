package ro.eduplatform.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ro.eduplatform.domain.AnalyticsEvent;
import ro.eduplatform.domain.Role;
import ro.eduplatform.domain.Submission;
import ro.eduplatform.domain.User;
import ro.eduplatform.dto.AdminDtos;
import ro.eduplatform.dto.AuthDtos;
import ro.eduplatform.repository.AnalyticsEventRepository;
import ro.eduplatform.repository.SubmissionRepository;
import ro.eduplatform.repository.TestRepository;
import ro.eduplatform.repository.UserRepository;
import ro.eduplatform.service.AnalyticsService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final SubmissionRepository submissionRepository;
    private final AnalyticsEventRepository analyticsEventRepository;
    private final AnalyticsService analyticsService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public List<AdminDtos.UserDto> users() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @PostMapping("/users")
    public AdminDtos.UserDto createUser(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ApiException(409, "Username deja folosit");
        }
        Role role = req.role() == null ? Role.STUDENT : req.role();
        if (role == Role.STUDENT && (req.gradeLevel() == null || req.gradeLevel() < 5 || req.gradeLevel() > 8)) {
            throw new ApiException(400, "Pentru elevi, clasa trebuie sa fie intre 5 si 8");
        }
        User u = User.builder()
                .username(req.username())
                .fullName(req.fullName())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(role)
                .gradeLevel(role == Role.STUDENT ? req.gradeLevel() : null)
                .build();
        return toDto(userRepository.save(u));
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new ApiException(404, "Utilizator inexistent"));
        userRepository.delete(u);
    }

    @PostMapping("/parents/{parentId}/children/{studentId}")
    public AdminDtos.UserDto linkChild(@PathVariable Long parentId, @PathVariable Long studentId) {
        User parent = userRepository.findById(parentId).orElseThrow(() -> new ApiException(404, "Parinte inexistent"));
        User student = userRepository.findById(studentId).orElseThrow(() -> new ApiException(404, "Elev inexistent"));
        if (parent.getRole() != Role.PARENT) throw new ApiException(400, "Utilizatorul nu este parinte");
        if (student.getRole() != Role.STUDENT) throw new ApiException(400, "Tinta nu este elev");
        parent.getChildren().add(student);
        userRepository.save(parent);
        return toDto(parent);
    }

    @DeleteMapping("/parents/{parentId}/children/{studentId}")
    public void unlinkChild(@PathVariable Long parentId, @PathVariable Long studentId) {
        User parent = userRepository.findById(parentId).orElseThrow(() -> new ApiException(404, "Parinte inexistent"));
        parent.getChildren().removeIf(c -> c.getId().equals(studentId));
        userRepository.save(parent);
    }

    @GetMapping("/analytics")
    public AdminDtos.AnalyticsResponse analytics() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.findByRole(Role.STUDENT).size();
        long totalTeachers = userRepository.findByRole(Role.TEACHER).size();
        long totalParents = userRepository.findByRole(Role.PARENT).size();
        long totalAdmins = userRepository.findByRole(Role.ADMIN).size();
        long totalTests = testRepository.count();
        long totalSubmissions = submissionRepository.count();

        Map<String, Long> studentsByGrade = new LinkedHashMap<>();
        for (User u : userRepository.findByRole(Role.STUDENT)) {
            String key = u.getGradeLevel() == null ? "?" : ("clasa " + u.getGradeLevel());
            studentsByGrade.merge(key, 1L, Long::sum);
        }

        Map<String, Long> testsByGrade = new LinkedHashMap<>();
        testRepository.findAll().forEach(t ->
                testsByGrade.merge("clasa " + t.getGradeLevel(), 1L, Long::sum));

        Map<String, Long> eventCounts = new LinkedHashMap<>();
        analyticsService.all().forEach(e ->
                eventCounts.merge(e.getEventType(), 1L, Long::sum));

        double avg = submissionRepository.findAll().stream()
                .mapToDouble(Submission::getGrade).average().orElse(0.0);

        List<AdminDtos.RecentEvent> recent = analyticsService.recent().stream()
                .map(this::toRecent).toList();

        return new AdminDtos.AnalyticsResponse(
                totalUsers, totalStudents, totalTeachers, totalParents, totalAdmins,
                totalTests, totalSubmissions,
                studentsByGrade, testsByGrade, eventCounts,
                Math.round(avg * 100.0) / 100.0,
                recent
        );
    }

    @GetMapping("/events")
    public List<AdminDtos.RecentEvent> events() {
        return analyticsService.recent().stream().map(this::toRecent).toList();
    }

    private AdminDtos.RecentEvent toRecent(AnalyticsEvent e) {
        return new AdminDtos.RecentEvent(
                e.getId(),
                e.getUser() != null ? e.getUser().getUsername() : "(anonim)",
                e.getEventType(),
                e.getTestId(),
                e.getOccurredAt()
        );
    }

    private AdminDtos.UserDto toDto(User u) {
        return new AdminDtos.UserDto(
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getRole(),
                u.getGradeLevel(),
                u.getCreatedAt(),
                u.getChildren() == null ? List.of() : u.getChildren().stream().map(User::getId).toList()
        );
    }
}
