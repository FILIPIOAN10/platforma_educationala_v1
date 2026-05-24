package ro.eduplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.eduplatform.domain.AnalyticsEvent;

import java.util.List;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    long countByEventType(String eventType);

    List<AnalyticsEvent> findTop100ByOrderByOccurredAtDesc();
}
