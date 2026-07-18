package com.mohamed.coursemanagement.serviceImpl;

import com.mohamed.coursemanagement.dto.EnrollmentRequestDto;
import com.mohamed.coursemanagement.dto.EnrollmentResponseDto;
import com.mohamed.coursemanagement.entity.Course;
import com.mohamed.coursemanagement.entity.Enrollment;
import com.mohamed.coursemanagement.entity.Instructor;
import com.mohamed.coursemanagement.entity.Student;
import com.mohamed.coursemanagement.exception.DuplicateResourceException;
import com.mohamed.coursemanagement.exception.RegistrationClosedException;
import com.mohamed.coursemanagement.exception.ResourceNotFoundException;
import com.mohamed.coursemanagement.repository.CourseRepository;
import com.mohamed.coursemanagement.repository.EnrollmentRepository;
import com.mohamed.coursemanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import com.mohamed.coursemanagement.mapper.EnrollmentMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Spy
    private EnrollmentMapper enrollmentMapper;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private Student student;
    private Course course;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L)
                .name("Mohamed Ayman")
                .email("mohamed@student.edu")
                .build();

        Instructor instructor = Instructor.builder()
                .id(1L)
                .name("Dr. Ahmed Hassan")
                .email("ahmed@uni.edu")
                .build();

        course = Course.builder()
                .id(1L)
                .title("Spring Boot 101")
                .credits(3)
                .instructor(instructor)
                .registrationStartTime(LocalDateTime.now().minusDays(1))
                .registrationEndTime(LocalDateTime.now().plusDays(7))
                .build();

        enrollment = Enrollment.builder()
                .id(1L)
                .student(student)
                .course(course)
                .enrolledAt(LocalDateTime.of(2026, 7, 11, 12, 0))
                .build();
    }

    @Test
    void enroll_createsEnrollmentAndReturnsDto() {
        EnrollmentRequestDto request = new EnrollmentRequestDto(1L, 1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponseDto result = enrollmentService.enroll(request);

        assertThat(result.studentName()).isEqualTo("Mohamed Ayman");
        assertThat(result.courseTitle()).isEqualTo("Spring Boot 101");
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enroll_throwsBadRequest_whenRegistrationHasNotOpenedYet() {
        course.setRegistrationStartTime(LocalDateTime.now().plusDays(2));
        course.setRegistrationEndTime(LocalDateTime.now().plusDays(9));
        EnrollmentRequestDto request = new EnrollmentRequestDto(1L, 1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> enrollmentService.enroll(request))
                .isInstanceOf(RegistrationClosedException.class)
                .hasMessageContaining("has not opened yet");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enroll_throwsBadRequest_whenRegistrationHasAlreadyClosed() {
        course.setRegistrationStartTime(LocalDateTime.now().minusDays(9));
        course.setRegistrationEndTime(LocalDateTime.now().minusDays(2));
        EnrollmentRequestDto request = new EnrollmentRequestDto(1L, 1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> enrollmentService.enroll(request))
                .isInstanceOf(RegistrationClosedException.class)
                .hasMessageContaining("has closed");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enroll_allowsCourseWithoutRegistrationWindow() {
        course.setRegistrationStartTime(null);
        course.setRegistrationEndTime(null);
        EnrollmentRequestDto request = new EnrollmentRequestDto(1L, 1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponseDto result = enrollmentService.enroll(request);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void enroll_throwsNotFound_whenStudentMissing() {
        EnrollmentRequestDto request = new EnrollmentRequestDto(99L, 1L);
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enroll(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student not found");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enroll_throwsNotFound_whenCourseMissingOrDeleted() {
        EnrollmentRequestDto request = new EnrollmentRequestDto(1L, 99L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enroll(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enroll_throwsDuplicate_whenAlreadyEnrolled() {
        EnrollmentRequestDto request = new EnrollmentRequestDto(1L, 1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enroll(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already enrolled");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void unenroll_deletesEnrollment_whenExists() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        enrollmentService.unenroll(1L);

        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    void unenroll_throwsNotFound_whenEnrollmentMissing() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.unenroll(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(enrollmentRepository, never()).delete(any());
    }

    @Test
    void getByStudent_returnsPage_whenStudentExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Enrollment> page = new PageImpl<>(List.of(enrollment), pageable, 1);
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findByStudentId(1L, pageable)).thenReturn(page);

        Page<EnrollmentResponseDto> result = enrollmentService.getByStudent(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().courseTitle()).isEqualTo("Spring Boot 101");
    }

    @Test
    void getByStudent_throwsNotFound_whenStudentMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> enrollmentService.getByStudent(99L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByCourse_returnsPage_whenCourseExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Enrollment> page = new PageImpl<>(List.of(enrollment), pageable, 1);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findByCourseId(1L, pageable)).thenReturn(page);

        Page<EnrollmentResponseDto> result = enrollmentService.getByCourse(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().studentName()).isEqualTo("Mohamed Ayman");
    }

    @Test
    void getByCourse_throwsNotFound_whenCourseMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> enrollmentService.getByCourse(99L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
