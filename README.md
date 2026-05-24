# EduPlatform - platforma de teste pentru clasele V-VIII

Platforma educationala inspirata de EduBoom, dedicata **exclusiv testelor**, pentru elevi romani din ciclul gimnazial (clasele V-VIII). Implementata ca MVP cu autentificare JWT in cookie HTTP-only, RBAC si baza de date H2 (in-memory).

## Roluri (RBAC)

| Rol | Ce poate face |
|-----|---------------|
| **STUDENT** (elev) | Vede teste pentru clasa lui, rezolva teste, primeste nota automata; **cand greseste o intrebare i se afiseaza raspunsul corect + explicatia**; vede istoric/progres |
| **TEACHER** (profesor) | Creeaza/editeaza/sterge teste, vede rezultatele elevilor, dashboard cu medii pe materie/test |
| **ADMIN** | Gestioneaza utilizatorii (CRUD), creeaza/sterge orice test, vede analytics: distributie pe clase, engagement (clicks/views), evenimente recente |
| **PARENT** (parinte) | Dashboard simplificat al copilului: medie generala, evolutie in timp, materii tari/slabe, istoric note (read-only) |

## Stack

- **Backend**: Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA, H2, JJWT
- **Frontend**: React 18 + Vite + React Router 6 (SPA, fetch + cookies)
- **Auth**: JWT semnat HS512, transportat in cookie HTTP-only `edu_token`

## Structura proiect

```
backend/    - aplicatia Spring Boot (cu Maven Wrapper inclus, mvnw / mvnw.cmd)
frontend/   - aplicatia React (Vite)
```

## Cum rulezi local

### Cerinte minime

- **JDK 17 sau 21** instalat — verifica cu `java -version`
- **Node.js 18+** si `npm` — verifica cu `node --version`
- **NU ai nevoie de Maven** instalat — repo-ul include Maven Wrapper (`mvnw` / `mvnw.cmd`)

---

### 1. Backend (port 8080)

#### Varianta A - IntelliJ IDEA (recomandat)

1. **File -> Open** si selecteaza folderul **`backend`** (NU folderul radacina al repo-ului). IntelliJ recunoaste automat proiectul Maven din `pom.xml`.
2. Daca IntelliJ afiseaza un banner "Maven projects need to be imported", click pe **Load Maven Project** / **Import Maven Projects**. Asteapta sa se descarce dependentele (bara de progres jos).
3. **File -> Project Structure -> Project**: seteaza **SDK = 17** (sau 21). Daca lipseste: *Add SDK -> Download JDK -> Eclipse Temurin 17*. Seteaza si **Language level = 17**.
4. **Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors**: bifeaza **Enable annotation processing** (necesar pentru Lombok).
5. **Settings -> Plugins**: pluginul **Lombok** trebuie sa fie instalat si activ (este bundled in IntelliJ recente).
6. Deschide `src/main/java/ro/eduplatform/EduPlatformApplication.java` -> click pe sageata verde din stanga lui `public class EduPlatformApplication` -> **Run 'EduPlatformApplication.main()'**.
7. In consola vei vedea `Started EduPlatformApplication ... Tomcat started on port 8080`. Backend-ul ruleaza la http://localhost:8080.

#### Varianta B - din linia de comanda (fara IDE)

**Linux / macOS:**

```bash
cd backend
chmod +x mvnw          # doar prima data
./mvnw -DskipTests package
java -jar target/eduplatform-backend-0.0.1-SNAPSHOT.jar
```

**Windows (PowerShell sau CMD):**

```cmd
cd backend
mvnw.cmd -DskipTests package
java -jar target\eduplatform-backend-0.0.1-SNAPSHOT.jar
```

Console H2 (debug): http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:eduplatform`, user `sa`, fara parola).

---

### 2. Frontend (port 5173)

In alt terminal, in paralel cu backend-ul:

```bash
cd frontend
npm install
npm run dev
```

Aplicatia se deschide la http://localhost:5173. Vite proxy redirectioneaza automat orice request `/api/...` catre backend-ul de pe `:8080`.

---

### Probleme frecvente

| Problema | Solutie |
|----------|---------|
| **IntelliJ nu imi vede `pom.xml` / nu apare ca proiect Maven** | Ai deschis folderul gresit. Inchide proiectul si `File -> Open` -> alege exact folderul **`backend`** (cel care contine `pom.xml`), nu radacina repo-ului. Alternativ: `File -> New -> Project from Existing Sources` -> selecteaza `backend/pom.xml` -> *Import project from external model: Maven*. |
| **`No JDK configured` sau `invalid target release: 17`** | `File -> Project Structure -> Project` -> SDK = 17 sau 21. Daca nu apare in lista, *Add SDK -> Download JDK -> Temurin 17*. |
| **Lombok: `cannot find symbol getId()` / `builder()`** | Instaleaza pluginul **Lombok** si bifeaza **Enable annotation processing**. Apoi `Build -> Rebuild Project`. |
| **`./mvnw: Permission denied` (Linux/macOS)** | `chmod +x mvnw` |
| **`mvnw : The term 'mvnw' is not recognized` (PowerShell)** | Foloseste `.\mvnw.cmd` (cu `.\` in fata) sau `mvnw.cmd`. |
| **Port 8080 ocupat** | In `backend/src/main/resources/application.yml` schimba `server.port`. In `frontend/vite.config.js` schimba `target` din `proxy` la noul port. |
| **Frontend ramane pe pagina de login (401)** | Backend-ul nu ruleaza pe `:8080`. Porneste-l intai. Foloseste `npm run dev`, nu `npm run preview`. |
| **`npm install` esueaza** | Sterge `frontend/node_modules` si `frontend/package-lock.json`, apoi reincearca. |
| **Eroare la primul build cu wrapper: `Could not find or load main class`** | E posibil sa ai un JDK foarte vechi. Verifica `java -version` -> trebuie >=17. |

---

## Conturi demo (create automat la prima pornire)

| Rol | Username | Parola |
|-----|----------|--------|
| Admin | `admin` | `admin123` |
| Profesor | `prof.matei`, `prof.popescu` | `prof123` |
| Elev clasa V | `elev5` | `elev123` |
| Elev clasa VI | `elev6` | `elev123` |
| Elev clasa VII | `elev7` | `elev123` |
| Elev clasa VIII | `elev8` | `elev123` |
| Parinte | `parinte` | `parinte123` (asociat cu `elev5` si `elev6`) |

Sunt 8 teste seed-uite pentru cele 4 clase x 2 materii (matematica + romana), cu cele 3 tipuri de intrebari:
- **Multiple choice** (variante grila)
- **Adevarat / Fals**
- **Raspuns scurt** (text liber)

## API principal

### Autentificare (`/api/auth`)
- `POST /login` - body `{username, password}` -> seteaza cookie + returneaza profil
- `POST /register` - creeaza cont (default STUDENT, cu `gradeLevel`)
- `POST /logout` - sterge cookie
- `GET /me` - profilul curent

### Elev (`/api/student`, doar `STUDENT`)
- `GET /tests` - lista teste pentru clasa elevului
- `GET /tests/{id}` - detalii test (fara raspunsuri corecte)
- `POST /submissions` - trimite test, primeste scor + **feedback cu raspunsul corect pentru fiecare intrebare gresita**
- `GET /submissions` - istoric
- `GET /submissions/{id}` - detalii rezolvare cu feedback

### Profesor (`/api/teacher`, `TEACHER` / `ADMIN`)
- `GET|POST /tests` - listeaza / creeaza teste
- `GET|PUT|DELETE /tests/{id}` - operatii pe test propriu (admin pe orice)
- `GET /tests/{id}/results` - rezultatele elevilor pe acel test
- `GET /results` - toate rezultatele pe testele profesorului
- `GET /dashboard` - agregat: total teste, total submisii, medie generala, medie pe materie / pe test

### Parinte (`/api/parent`, doar `PARENT`)
- `GET /children` - copiii asociati
- `GET /dashboard` - pentru fiecare copil: medie generala, medii pe materie, materii tari/slabe, evolutie zilnica, ultimele rezultate
- `GET /children/{id}/progress` - acelasi payload doar pentru un copil

### Admin (`/api/admin`, doar `ADMIN`)
- `GET|POST /users` - lista / creare utilizator (orice rol)
- `DELETE /users/{id}`
- `POST /parents/{parentId}/children/{studentId}` - leaga parinte-elev
- `DELETE /parents/{parentId}/children/{studentId}` - dezleaga
- `GET /analytics` - totaluri, distributie elevi/teste pe clasa, engagement (counter pe `eventType`), media generala, evenimente recente
- `GET /events` - ultimele 100 evenimente

## Securitate

- Spring Security in mod stateless, sesiune NU este creata
- Filtru custom (`JwtCookieAuthFilter`) extrage tokenul din cookie HTTP-only `edu_token` (fallback pe `Authorization: Bearer`)
- Parolele sunt salvate cu BCrypt
- RBAC declarativ pe controller (`@PreAuthorize` + `requestMatchers` cu `hasRole(...)`)
- Validare suplimentara la nivel de business: un elev nu poate accesa testele altei clase, un profesor nu poate edita un test pe care nu l-a creat (admin poate)
- Pentru productie: seteaza `secure(true)` si `SameSite=None` pe cookie, inlocuieste `app.jwt.secret`

## Fluxul "raspuns corect afisat la greseala"

In `SubmissionService.submit(...)` fiecare intrebare este evaluata:
- pentru **MULTIPLE_CHOICE** se compara id-ul optiunii alese cu optiunea marcata `correct=true`
- pentru **TRUE_FALSE** se compara textul `true`/`false` cu `correctAnswerText`
- pentru **SHORT_ANSWER** se face match case-insensitive pe `correctAnswerText`

Raspunsul catre client (`SubmissionResult`) contine o lista `feedback[]` in care, pentru fiecare intrebare:
- `givenAnswer` - ce a raspuns elevul (text uman, nu id),
- `correctAnswer` - raspunsul corect (text uman),
- `correct` - true/false,
- `pointsEarned` / `pointsMax`,
- `explanation` - comentariul optional setat de profesor.

Frontend-ul afiseaza un panel verde daca a fost corect, sau rosu cu banner galben "Raspuns corect: ..." pe intrebarile gresite, plus explicatia.

## Imagini scanate (din culegeri / manual)

Fiecare intrebare are un camp optional `imageUrl`. La crearea / editarea testului, profesorul poate atasa link-ul (sau servire statica ulterior) si completeaza manual enuntul + raspunsul. UI-ul afiseaza imaginea deasupra optiunilor.

> Extensii viitoare prevazute: OCR + AI pentru extragere automata din PDF / poze.

## Calcul nota

`grade = 1 + 9 * (puncte_obtinute / puncte_max)`, rotunjit la 2 zecimale -> scala 1-10 stil romanesc.

## Comenzi rapide

```bash
# build complet
cd backend && ./mvnw -DskipTests package
cd ../frontend && npm install && npm run build

# ruleaza backend
cd backend && java -jar target/eduplatform-backend-0.0.1-SNAPSHOT.jar

# ruleaza frontend dev
cd frontend && npm run dev
```
