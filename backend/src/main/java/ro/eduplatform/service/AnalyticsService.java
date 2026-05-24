package ro.eduplatform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.eduplatform.domain.AnalyticsEvent;
import ro.eduplatform.domain.User;
import ro.eduplatform.repository.AnalyticsEventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsEventRepository repository;

    public void record(User user, String eventType, Long testId, String metadata) {
        AnalyticsEvent ev = AnalyticsEvent.builder()
                .user(user)
                .eventType(eventType)
                .testId(testId)
                .metadata(metadata)
                .build();
        repository.save(ev);
    }

    public List<AnalyticsEvent> recent() {
        return repository.findTop100ByOrderByOccurredAtDesc();
    }

    public List<AnalyticsEvent> all() {
        return repository.findAll();
    }
}
