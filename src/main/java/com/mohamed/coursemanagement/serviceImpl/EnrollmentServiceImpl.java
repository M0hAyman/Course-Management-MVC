package com.mohamed.coursemanagement.serviceImpl;

import com.mohamed.coursemanagement.dto.EnrollmentRequestDto;
import com.mohamed.coursemanagement.dto.EnrollmentResponseDto;
import com.mohamed.coursemanagement.entity.Course;
import com.mohamed.coursemanagement.entity.Enrollment;
import com.mohamed.coursemanagement.entity.Student;
import com.mohamed.coursemanagement.exception.DuplicateResourceException;
import com.mohamed.coursemanagement.exception.RegistrationClosedException;
import com.mohamed.coursemanagement.exception.ResourceNotFoundException;
import com.mohamed.coursemanagement.mapper.EnrollmentMapper;
import com.mohamed.coursemanagement.repository.CourseRepository;
import com.mohamed.coursemanagement.repository.EnrollmentRepository;
import com.mohamed.coursemanagement.repository.StudentRepository;
import com.mohamed.coursemanagement.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    public EnrollmentResponseDto enroll(EnrollmentRequestDto request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.studentId()));

        Course course = courseRepository.findByIdAndDeletedFalse(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.courseId()));

        validateRegistrationWindow(course);

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new DuplicateResourceException(
                    "Student " + student.getId() + " is already enrolled in course " + course.getId());
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        return enrollmentMapper.toDto(saved);
    }

    private void validateRegistrationWindow(Course course) {
        LocalDateTime now = LocalDateTime.now();

        if (course.getRegistrationStartTime() != null && now.isBefore(course.getRegistrationStartTime())) {
            throw new RegistrationClosedException(
                    "Registration for course '" + course.getTitle() + "' has not opened yet. It opens at "
                            + course.getRegistrationStartTime());
        }
        if (course.getRegistrationEndTime() != null && now.isAfter(course.getRegistrationEndTime())) {
            throw new RegistrationClosedException(
                    "Registration for course '" + course.getTitle() + "' has closed. It ended at "
                            + course.getRegistrationEndTime());
        }
    }

    @Override
    public void unenroll(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));
        enrollmentRepository.delete(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getByStudent(Long studentId, Pageable pageable) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        return enrollmentRepository.findByStudentId(studentId, pageable).map(enrollmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getByCourse(Long courseId, Pageable pageable) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        return enrollmentRepository.findByCourseId(courseId, pageable).map(enrollmentMapper::toDto);
    }

}
