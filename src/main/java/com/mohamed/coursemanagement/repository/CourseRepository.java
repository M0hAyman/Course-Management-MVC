package com.mohamed.coursemanagement.repository;

import com.mohamed.coursemanagement.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByDeletedFalse(Pageable pageable);

    Optional<Course> findByIdAndDeletedFalse(Long id);

    Page<Course> findByInstructorIdAndDeletedFalse(Long instructorId, Pageable pageable);
}
