package ro.eduplatform.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "submission_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    /**
     * Pentru SHORT_ANSWER si TRUE_FALSE: textul raspunsului introdus.
     * Pentru MULTIPLE_CHOICE: id-ul optiunii alese (string).
     */
    @Column(length = 1000)
    private String givenAnswer;

    @Column(nullable = false)
    private Boolean correct;

    @Column(nullable = false)
    private Integer pointsEarned;
}
