package ro.eduplatform.dto;

import ro.eduplatform.domain.Role;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class AdminDtos {

    public record UserDto(
            Long id,
            String username,
            String fullName,
            String email,
            Role role,
            Integer gradeLevel,
            Instant createdAt,
            List<Long> childrenIds
    ) {}

    public record AnalyticsResponse(
            long totalUsers,
            long totalStudents,
            long totalTeachers,
            long totalParents,
            long totalAdmins,
            long totalTests,
            long totalSubmissions,
            Map<String, Long> studentsByGrade,
            Map<String, Long> testsByGrade,
            Map<String, Long> eventCounts,
            double averageGrade,
            List<RecentEvent> recentEvents
    ) {}

    public record RecentEvent(
            Long id,
            String username,
            String eventType,
            Long testId,
            Instant occurredAt
    ) {}
}
