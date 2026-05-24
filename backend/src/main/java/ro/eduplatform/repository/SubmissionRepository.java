package ro.eduplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.eduplatform.domain.Submission;
import ro.eduplatform.domain.TestEntity;
import ro.eduplatform.domain.User;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByStudentOrderBySubmittedAtDesc(User student);

    List<Submission> findByTestOrderBySubmittedAtDesc(TestEntity test);

    List<Submission> findByStudentInOrderBySubmittedAtDesc(List<User> students);
}
