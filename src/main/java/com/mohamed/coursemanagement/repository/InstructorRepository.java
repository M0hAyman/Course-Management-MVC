package com.mohamed.coursemanagement.repository;

import com.mohamed.coursemanagement.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    Optional<Instructor> findByEmail(String email);

    boolean existsByEmail(String email);
}
