# Course Management System — MVC Twin

A **monolithic Spring MVC + Thymeleaf** implementation of the Course Management System, built as the bonus twin of the [multi-service REST version](https://github.com/M0hAyman/Course-Management-System). Same domain, same business rules — but instead of returning JSON to API clients, the server renders complete HTML pages.

Part of the **Innovera internship** program.

## What's different from the REST version?

| | REST version | This MVC twin |
|---|---|---|
| Architecture | 2 services + PostgreSQL + Docker | one monolith, H2 in-memory |
| Controllers | `@RestController` returns JSON | `@Controller` returns view names |
| Presentation | client's responsibility | server-rendered Thymeleaf templates |
| Input | JSON bodies → DTO records | HTML forms → form backing beans |
| Errors | JSON + HTTP status | flash messages / error pages |

The service, repository, entity, mapper, and DTO layers are **ported unchanged** from the REST version — demonstrating that a clean layered architecture lets you swap the presentation layer without touching business logic. All 47 service-layer unit tests came along unchanged too.

## Features

**Public pages**
- Course catalog with live registration-status badges (OPEN / OPENS SOON / CLOSED)
- Course detail with an enroll form and the list of enrolled students
- Student registration, editing, and per-student enrollment list with unenroll

**Admin pages**
- Course management: create/edit with `datetime-local` registration-window inputs, soft delete
- Instructor management: full CRUD
- Enrollment report: per-course enrollment counts

**Business rules (same as the REST version)**
- Enrollment allowed only inside the course's registration window
- Duplicate emails and duplicate enrollments rejected
- Soft delete for courses — hidden from the catalog, history preserved
- Validation with per-field error messages rendered next to form inputs

## Tech Stack

Java 25 · Spring Boot 4.1 · Spring Web MVC · **Thymeleaf** · Spring Data JPA (Hibernate) · H2 (in-memory) · Jakarta Bean Validation · Lombok · JUnit 5 + Mockito + AssertJ · Maven

## Run

No database setup needed — H2 runs in memory and sample data is seeded on startup (2 instructors, 3 courses covering all registration-window states, 3 students, 3 enrollments).

**With Docker (nothing else required — no JDK, no Maven):**

```bash
docker-compose up --build
```

**Or locally (requires JDK 25):**

```bash
mvnw.cmd spring-boot:run     # Windows
./mvnw spring-boot:run       # Linux / macOS
```

Then open **http://localhost:8080** in a browser:

| Page | URL |
|---|---|
| Course catalog | `/courses` |
| Students | `/students` |
| Admin: manage courses | `/admin/courses` |
| Admin: instructors | `/admin/instructors` |
| Admin: enrollment report | `/admin/reports` |

## Tests

```bash
mvnw.cmd test
```

47 service-layer unit tests (JUnit 5 + Mockito), identical to the REST version's — the layer under test didn't change.

## Structure

```
src/main/java/com/mohamed/coursemanagement
├── controller      MVC controllers (return view names, not JSON)
├── form            form backing beans (mutable, validated, bound to HTML forms)
├── service         service interfaces
├── serviceImpl     service implementations (ported unchanged)
├── repository      Spring Data JPA repositories
├── entity          JPA entities
├── dto             DTO records used between controller and service
├── mapper          entity <-> DTO mappers
├── exception       domain exceptions + MVC error handling
└── config          idempotent data seeder
src/main/resources/templates    Thymeleaf views (fragments, public pages, admin pages)
```

## MVC patterns demonstrated

- **Post/Redirect/Get** — every successful form POST redirects, so refreshing the page never re-submits the form
- **Flash attributes** — one-shot success/error messages that survive the redirect
- **Form backing beans** — mutable objects bound with `th:object`/`th:field`, converted to immutable DTO records at the service boundary
- **Fragments** — shared nav/head/flash markup included by every page (`th:replace`)
- **BindingResult** — validation errors rendered inline next to the offending field, including business errors (duplicate email, invalid window) surfaced onto the form
