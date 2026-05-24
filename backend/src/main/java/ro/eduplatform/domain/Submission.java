package ro.eduplatform.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id")
    private TestEntity test;

    @Column(nullable = false)
    private Instant submittedAt;

    /**
     * Suma punctelor obtinute.
     */
    @Column(nullable = false)
    private Integer score;

    /**
     * Suma punctelor maxime ale testului la momentul submit-ului.
     */
    @Column(nullable = false)
    private Integer maxScore;

    /**
     * Nota pe scara 1-10.
     */
    @Column(nullable = false)
    private Double grade;

    /**
     * Timpul petrecut (in secunde).
     */
    private Integer timeSpentSeconds;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubmissionAnswer> answers = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (submittedAt == null) {
            submittedAt = Instant.now();
        }
    }
}
