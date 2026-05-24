package ro.eduplatform.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id")
    private TestEntity test;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;

    @Column(nullable = false, length = 2000)
    private String prompt;

    /**
     * Imagine pentru exercitii scanate (culegeri, manual). Optional.
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * Pentru SHORT_ANSWER: textul raspunsului corect (case-insensitive trim).
     * Pentru TRUE_FALSE: "true" sau "false".
     * Pentru MULTIPLE_CHOICE: poate fi gol; corect se ia din optiuni.
     */
    @Column(length = 500)
    private String correctAnswerText;

    /**
     * Explicatia / feedback-ul afisat dupa raspuns.
     */
    @Column(length = 2000)
    private String explanation;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 1;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<AnswerOption> options = new ArrayList<>();
}
