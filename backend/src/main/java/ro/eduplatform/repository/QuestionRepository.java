package ro.eduplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.eduplatform.domain.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
