package ro.eduplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.eduplatform.domain.TestEntity;
import ro.eduplatform.domain.User;

import java.util.List;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
    List<TestEntity> findByGradeLevel(Integer gradeLevel);

    List<TestEntity> findByCreator(User creator);
}
