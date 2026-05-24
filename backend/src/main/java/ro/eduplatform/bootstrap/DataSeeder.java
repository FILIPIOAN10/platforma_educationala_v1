package ro.eduplatform.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.eduplatform.domain.*;
import ro.eduplatform.repository.TestRepository;
import ro.eduplatform.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;
        log.info("Seeding date initiale...");

        User admin = save("admin", "Administrator Sistem", "admin@edu.ro", "admin123", Role.ADMIN, null);
        User profMate = save("prof.matei", "Prof. Mihai Matei", "matei@edu.ro", "prof123", Role.TEACHER, null);
        User profRom = save("prof.popescu", "Prof. Ana Popescu", "popescu@edu.ro", "prof123", Role.TEACHER, null);

        User elev5 = save("elev5", "Andrei Ionescu", "andrei@edu.ro", "elev123", Role.STUDENT, 5);
        User elev6 = save("elev6", "Maria Pop", "maria@edu.ro", "elev123", Role.STUDENT, 6);
        User elev7 = save("elev7", "Vlad Stan", "vlad@edu.ro", "elev123", Role.STUDENT, 7);
        User elev8 = save("elev8", "Ioana Radu", "ioana@edu.ro", "elev123", Role.STUDENT, 8);

        User parinte = save("parinte", "Mihaela Ionescu", "parinte@edu.ro", "parinte123", Role.PARENT, null);
        parinte.getChildren().add(elev5);
        parinte.getChildren().add(elev6);
        userRepository.save(parinte);

        seedClasaV(profMate, profRom);
        seedClasaVI(profMate, profRom);
        seedClasaVII(profMate, profRom);
        seedClasaVIII(profMate, profRom);

        log.info("Seed complet. Conturi: admin/admin123, prof.matei/prof123, elev5..elev8/elev123, parinte/parinte123");
    }

    private User save(String username, String name, String email, String pw, Role role, Integer grade) {
        User u = User.builder()
                .username(username).fullName(name).email(email)
                .passwordHash(encoder.encode(pw))
                .role(role)
                .gradeLevel(grade)
                .build();
        return userRepository.save(u);
    }

    private void seedClasaV(User profMate, User profRom) {
        TestEntity t1 = TestEntity.builder()
                .title("Operatii cu numere naturale")
                .description("Test introductiv pentru clasa a V-a la matematica")
                .subject(Subject.MATEMATICA)
                .gradeLevel(5)
                .chapter("Numere naturale")
                .difficulty(Difficulty.USOR)
                .creator(profMate)
                .questions(new ArrayList<>())
                .build();
        addMC(t1, "Cat este 12 + 8?", List.of("18", "20", "22", "24"), 1, "Adunare simpla: 12+8=20.");
        addMC(t1, "Cat este 7 x 6?", List.of("36", "42", "48", "56"), 1, "Tabla inmultirii: 7*6=42.");
        addTF(t1, "Numarul 0 este numar natural.", true, "Multimea N include si 0 in conventia romaneasca.");
        addShort(t1, "Cat este 100 - 37?", "63", "Scadere: 100-37=63.");
        testRepository.save(t1);

        TestEntity t2 = TestEntity.builder()
                .title("Substantivul - notiuni de baza")
                .description("Test scurt despre substantive")
                .subject(Subject.ROMANA)
                .gradeLevel(5)
                .chapter("Morfologia - substantivul")
                .difficulty(Difficulty.USOR)
                .creator(profRom)
                .questions(new ArrayList<>())
                .build();
        addMC(t2, "Care dintre cuvinte este substantiv?",
                List.of("frumos", "carte", "alearga", "repede"), 1,
                "Substantivul denumeste fiinte, lucruri, fenomene.");
        addTF(t2, "Substantivul propriu se scrie cu litera mare.", true,
                "Numele proprii se scriu intotdeauna cu majuscula.");
        addShort(t2, "Care este pluralul cuvantului 'copil'?", "copii", "Plural: copii.");
        testRepository.save(t2);
    }

    private void seedClasaVI(User profMate, User profRom) {
        TestEntity t1 = TestEntity.builder()
                .title("Fractii ordinare")
                .description("Operatii cu fractii pentru clasa a VI-a")
                .subject(Subject.MATEMATICA)
                .gradeLevel(6)
                .chapter("Fractii")
                .difficulty(Difficulty.MEDIU)
                .creator(profMate)
                .questions(new ArrayList<>())
                .build();
        addMC(t1, "Cat este 1/2 + 1/4?", List.of("2/6", "3/4", "1/3", "2/4"), 1,
                "Aducere la acelasi numitor: 2/4 + 1/4 = 3/4.");
        addMC(t1, "Care fractie este mai mare?", List.of("2/3", "3/5", "1/2", "4/9"), 0,
                "2/3 ≈ 0.66 este cea mai mare.");
        addShort(t1, "Simplifica fractia 6/9.", "2/3", "Impartim cu 3.");
        addTF(t1, "Fractia 5/5 este egala cu 1.", true, "Numarator = numitor => 1.");
        testRepository.save(t1);

        TestEntity t2 = TestEntity.builder()
                .title("Verbul - moduri si timpuri")
                .description("Test de morfologie - verbul")
                .subject(Subject.ROMANA)
                .gradeLevel(6)
                .chapter("Morfologia - verbul")
                .difficulty(Difficulty.MEDIU)
                .creator(profRom)
                .questions(new ArrayList<>())
                .build();
        addMC(t2, "Forma 'voi merge' este la timpul:",
                List.of("prezent", "imperfect", "viitor", "perfect compus"), 2,
                "Auxiliarul 'voi' indica viitorul.");
        addTF(t2, "Verbul 'a fi' este verb auxiliar.", true,
                "Este auxiliar pentru diateza pasiva.");
        testRepository.save(t2);
    }

    private void seedClasaVII(User profMate, User profRom) {
        TestEntity t1 = TestEntity.builder()
                .title("Ecuatii de gradul I")
                .description("Rezolvare de ecuatii liniare")
                .subject(Subject.MATEMATICA)
                .gradeLevel(7)
                .chapter("Ecuatii")
                .difficulty(Difficulty.MEDIU)
                .creator(profMate)
                .questions(new ArrayList<>())
                .build();
        addShort(t1, "Rezolva: 2x + 3 = 11. x = ?", "4", "2x = 8 => x = 4.");
        addMC(t1, "Solutia ecuatiei 5x = 20 este:",
                List.of("3", "4", "5", "6"), 1, "x = 20/5 = 4.");
        addTF(t1, "Orice ecuatie de gradul I are exact o solutie.", true,
                "Daca a ≠ 0, ax + b = 0 are o singura solutie.");
        testRepository.save(t1);

        TestEntity t2 = TestEntity.builder()
                .title("Texte narative - elemente de constructie")
                .subject(Subject.ROMANA)
                .gradeLevel(7)
                .chapter("Textul narativ")
                .difficulty(Difficulty.MEDIU)
                .creator(profRom)
                .questions(new ArrayList<>())
                .build();
        addMC(t2, "Cine este 'naratorul' in textul narativ?",
                List.of("personajul principal", "cel care povesteste actiunea",
                        "autorul cartii", "personajul antagonist"), 1,
                "Naratorul este vocea care povesteste actiunea.");
        addShort(t2, "Cum se numeste persoana 'eu, tu, el' folosita de narator?", "persoana",
                "Tipic: persoana I, a II-a sau a III-a.");
        testRepository.save(t2);
    }

    private void seedClasaVIII(User profMate, User profRom) {
        TestEntity t1 = TestEntity.builder()
                .title("Teorema lui Pitagora")
                .description("Aplicatii ale teoremei lui Pitagora")
                .subject(Subject.MATEMATICA)
                .gradeLevel(8)
                .chapter("Geometrie - triunghiul dreptunghic")
                .difficulty(Difficulty.GREU)
                .creator(profMate)
                .questions(new ArrayList<>())
                .build();
        addMC(t1, "Intr-un triunghi dreptunghic cu catetele 3 si 4, ipotenuza este:",
                List.of("5", "6", "7", "12"), 0,
                "3^2 + 4^2 = 9 + 16 = 25 = 5^2.");
        addShort(t1, "Catetele sunt 6 si 8. Cat este ipotenuza?", "10",
                "sqrt(36 + 64) = sqrt(100) = 10.");
        addTF(t1, "Teorema lui Pitagora este valabila in orice triunghi.", false,
                "Doar in triunghi dreptunghic.");
        testRepository.save(t1);

        TestEntity t2 = TestEntity.builder()
                .title("Figuri de stil")
                .subject(Subject.ROMANA)
                .gradeLevel(8)
                .chapter("Stilistica")
                .difficulty(Difficulty.MEDIU)
                .creator(profRom)
                .questions(new ArrayList<>())
                .build();
        addMC(t2, "'Luna ride pe cer' este un exemplu de:",
                List.of("metafora", "personificare", "comparatie", "epitet"), 1,
                "Atribuirea actiunii umane unui obiect = personificare.");
        addMC(t2, "'Alb ca zapada' este:",
                List.of("metafora", "personificare", "comparatie", "epitet"), 2,
                "Cuvantul 'ca' marcheaza o comparatie.");
        addShort(t2, "Cum se numeste figura de stil din 'foc rece'?", "oximoron",
                "Alaturare de termeni opusi = oximoron.");
        testRepository.save(t2);
    }

    private void addMC(TestEntity test, String prompt, List<String> options, int correctIndex, String explanation) {
        Question q = Question.builder()
                .test(test).type(QuestionType.MULTIPLE_CHOICE).prompt(prompt)
                .explanation(explanation)
                .points(1)
                .orderIndex(test.getQuestions().size())
                .options(new ArrayList<>())
                .build();
        for (int i = 0; i < options.size(); i++) {
            q.getOptions().add(AnswerOption.builder()
                    .question(q).text(options.get(i)).correct(i == correctIndex).orderIndex(i).build());
        }
        test.getQuestions().add(q);
    }

    private void addTF(TestEntity test, String prompt, boolean correct, String explanation) {
        Question q = Question.builder()
                .test(test).type(QuestionType.TRUE_FALSE).prompt(prompt)
                .correctAnswerText(correct ? "true" : "false")
                .explanation(explanation)
                .points(1)
                .orderIndex(test.getQuestions().size())
                .options(new ArrayList<>())
                .build();
        test.getQuestions().add(q);
    }

    private void addShort(TestEntity test, String prompt, String answer, String explanation) {
        Question q = Question.builder()
                .test(test).type(QuestionType.SHORT_ANSWER).prompt(prompt)
                .correctAnswerText(answer)
                .explanation(explanation)
                .points(1)
                .orderIndex(test.getQuestions().size())
                .options(new ArrayList<>())
                .build();
        test.getQuestions().add(q);
    }
}
