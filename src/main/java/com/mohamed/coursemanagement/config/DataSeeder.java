package com.mohamed.coursemanagement.config;

import com.mohamed.coursemanagement.entity.Course;
import com.mohamed.coursemanagement.entity.Enrollment;
import com.mohamed.coursemanagement.entity.Instructor;
import com.mohamed.coursemanagement.entity.Student;
import com.mohamed.coursemanagement.repository.CourseRepository;
import com.mohamed.coursemanagement.repository.EnrollmentRepository;
import com.mohamed.coursemanagement.repository.InstructorRepository;
import com.mohamed.coursemanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (instructorRepository.count() > 0) {
            log.info("Database already contains data; skipping seeding");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        Instructor ahmed = instructorRepository.save(Instructor.builder()
                .name("Dr. Ahmed Hassan").email("ahmed.hassan@uni.edu").build());
        Instructor sara = instructorRepository.save(Instructor.builder()
                .name("Dr. Sara Ali").email("sara.ali@uni.edu").build());

        Course springBoot = courseRepository.save(Course.builder()
                .title("Spring Boot 101")
                .description("Build REST APIs with Spring Boot")
                .credits(3)
                .instructor(ahmed)
                .registrationStartTime(now.minusDays(7))
                .registrationEndTime(now.plusDays(21))
                .build());

        Course databases = courseRepository.save(Course.builder()
                .title("Database Fundamentals")
                .description("Relational modeling and SQL")
                .credits(4)
                .instructor(sara)
                .registrationStartTime(now.plusDays(14))
                .registrationEndTime(now.plusDays(45))
                .build());

        Course legacyJava = courseRepository.save(Course.builder()
                .title("Java Basics")
                .description("Introduction to Java, registration already closed")
                .credits(2)
                .instructor(ahmed)
                .registrationStartTime(now.minusDays(60))
                .registrationEndTime(now.minusDays(30))
                .build());

        Student mohamed = studentRepository.save(Student.builder()
                .name("Mohamed Ayman").email("mohamed.ayman@student.edu").build());
        Student laila = studentRepository.save(Student.builder()
                .name("Laila Omar").email("laila.omar@student.edu").build());
        studentRepository.save(Student.builder()
                .name("Omar Khaled").email("omar.khaled@student.edu").build());

        enrollmentRepository.save(Enrollment.builder().student(mohamed).course(springBoot).build());
        enrollmentRepository.save(Enrollment.builder().student(laila).course(springBoot).build());
        enrollmentRepository.save(Enrollment.builder().student(mohamed).course(legacyJava).build());

        log.info("Seeded 2 instructors, 3 courses (open / not-yet-open / closed registration), "
                + "3 students, and 3 enrollments");
    }
}
