# EduPlatform - platformă de teste pentru clasele V-VIII

Platformă educațională inspirată de EduBoom, dedicată **exclusiv testelor**, pentru elevi români din ciclul gimnazial (clasele V-VIII). Implementată ca MVP cu autentificare JWT în cookie HTTP-only, RBAC și bază de date H2 (in-memory).

## Roluri (RBAC)

| Rol | Ce poate face |
|-----|---------------|
| **STUDENT** (elev) | Vede teste pentru clasa lui, rezolvă teste, primește notă automată; **când greșește o întrebare, i se afișează răspunsul corect + explicația**; vede istoric/progres |
| **TEACHER** (profesor) | Creează/editează/șterge teste, vede rezultatele elevilor, dashboard cu medii pe materie/test |
| **ADMIN** | Gestionează utilizatorii (CRUD), creează/șterge orice test, vede analytics: distribuție pe clase, engagement (clicks/views), evenimente recente |
| **PARENT** (părinte) | Dashboard simplificat al copilului: medie generală, evoluție în timp, materii tari/slabe, istoric note (read-only) |

## Stack

- **Backend**: Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA, H2, JJWT
- **Frontend**: React 18 + Vite + React Router 6 (SPA, fetch + cookies)
- **Auth**: JWT semnat HS512, transportat în cookie HTTP-only `edu_token`

## Structură proiect

```
backend/   - aplicația Spring Boot
frontend/  - aplicația React (Vite)
```

## Cum rulezi local

### 1. Backend (port 8080)

```bash
cd backend
mvn -DskipTests package
java -jar target/eduplatform-backend-0.0.1-SNAPSHOT.jar
```

H2 console (debug): http://localhost:8080/h2-console (`jdbc:h2:mem:eduplatform`, user `sa`, fără parolă)

### 2. Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Aplicația se deschide la http://localhost:5173 (Vite proxy redirectează `/api` către backend).

## Conturi demo (create automat la prima pornire)

| Rol | Username | Parolă |
|-----|----------|--------|
| Admin | `admin` | `admin123` |
| Profesor | `prof.matei`, `prof.popescu` | `prof123` |
| Elev clasa V | `elev5` | `elev123` |
| Elev clasa VI | `elev6` | `elev123` |
| Elev clasa VII | `elev7` | `elev123` |
| Elev clasa VIII | `elev8` | `elev123` |
| Părinte | `parinte` | `parinte123` (asociat cu `elev5` și `elev6`) |

Sunt 8 teste seed-uite pentru cele 4 clase × 2 materii (matematică + română), cu cele 3 tipuri de întrebări:
- **Multiple choice** (variante grilă)
- **Adevărat / Fals**
- **Răspuns scurt** (text liber)

## API principal

### Autentificare (`/api/auth`)
- `POST /login` — body `{username, password}` → setează cookie + returnează profil
- `POST /register` — creează cont (default STUDENT, cu `gradeLevel`)
- `POST /logout` — șterge cookie
- `GET /me` — profilul curent

### Elev (`/api/student`, doar `STUDENT`)
- `GET /tests` — lista teste pentru clasa elevului
- `GET /tests/{id}` — detalii test (fără răspunsuri corecte)
- `POST /submissions` — trimite test, primește scor + **feedback cu răspunsul corect pentru fiecare întrebare greșită**
- `GET /submissions` — istoric
- `GET /submissions/{id}` — detalii rezolvare cu feedback

### Profesor (`/api/teacher`, `TEACHER` / `ADMIN`)
- `GET|POST /tests` — listează / creează teste
- `GET|PUT|DELETE /tests/{id}` — operații pe test propriu (admin pe orice)
- `GET /tests/{id}/results` — rezultatele elevilor pe acel test
- `GET /results` — toate rezultatele pe testele profesorului
- `GET /dashboard` — agregat: total teste, total submisii, medie generală, medie pe materie / pe test

### Părinte (`/api/parent`, doar `PARENT`)
- `GET /children` — copiii asociați
- `GET /dashboard` — pentru fiecare copil: medie generală, medii pe materie, materii tari/slabe, evoluție zilnică, ultimele rezultate
- `GET /children/{id}/progress` — același payload doar pentru un copil

### Admin (`/api/admin`, doar `ADMIN`)
- `GET|POST /users` — listă / creare utilizator (orice rol)
- `DELETE /users/{id}`
- `POST /parents/{parentId}/children/{studentId}` — leagă părinte-elev
- `DELETE /parents/{parentId}/children/{studentId}` — dezleagă
- `GET /analytics` — totaluri, distribuție elevi/teste pe clasă, engagement (counter pe `eventType`), media generală, evenimente recente
- `GET /events` — ultimele 100 evenimente

## Securitate

- Spring Security în mod stateless, sesiune NU este creată
- Filtru custom (`JwtCookieAuthFilter`) extrage tokenul din cookie HTTP-only `edu_token` (fallback pe `Authorization: Bearer`)
- Parolele sunt salvate cu BCrypt
- RBAC declarativ pe controller (`@PreAuthorize` + `requestMatchers` cu `hasRole(...)`)
- Validare suplimentară la nivel de business: un elev nu poate accesa testele altei clase, un profesor nu poate edita un test pe care nu l-a creat (admin poate)
- Pentru producție: setează `secure(true)` și `SameSite=None` pe cookie, înlocuiește `app.jwt.secret`

## Fluxul "răspuns corect afișat la greșeală"

În `SubmissionService.submit(...)` fiecare întrebare este evaluată:
- pentru **MULTIPLE_CHOICE** se compară id-ul opțiunii alese cu opțiunea marcată `correct=true`
- pentru **TRUE_FALSE** se compară textul `true`/`false` cu `correctAnswerText`
- pentru **SHORT_ANSWER** se face match case-insensitive pe `correctAnswerText`

Răspunsul către client (`SubmissionResult`) conține o listă `feedback[]` în care, pentru fiecare întrebare:
- `givenAnswer` — ce a răspuns elevul (text uman, nu id),
- `correctAnswer` — răspunsul corect (text uman),
- `correct` — true/false,
- `pointsEarned` / `pointsMax`,
- `explanation` — comentariul opțional setat de profesor.

Frontend-ul afișează un panel verde dacă a fost corect, sau roșu cu banner galben "Răspuns corect: ..." pe întrebările greșite, plus explicația.

## Imagini scanate (din culegeri / manual)

Fiecare întrebare are un câmp opțional `imageUrl`. La crearea / editarea testului, profesorul poate atașa link-ul (sau servire statică ulterior) și completează manual enunțul + răspunsul. UI-ul afișează imaginea deasupra opțiunilor.

> Extensii viitoare prevăzute: OCR + AI pentru extragere automată din PDF / poze (vezi specificație § 12).

## Calcul notă

`grade = 1 + 9 * (puncte_obtinute / puncte_max)`, rotunjit la 2 zecimale → scală 1-10 stil românesc.

## Comenzi rapide

```bash
# build complet
cd backend && mvn -DskipTests package
cd ../frontend && npm install && npm run build

# rulează backend
cd backend && java -jar target/eduplatform-backend-0.0.1-SNAPSHOT.jar

# rulează frontend dev
cd frontend && npm run dev
```
