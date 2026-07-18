package com.mohamed.coursemanagement.serviceImpl;

import com.mohamed.coursemanagement.dto.CourseRequestDto;
import com.mohamed.coursemanagement.dto.CourseResponseDto;
import com.mohamed.coursemanagement.entity.Course;
import com.mohamed.coursemanagement.entity.Instructor;
import com.mohamed.coursemanagement.exception.InvalidRegistrationWindowException;
import com.mohamed.coursemanagement.exception.ResourceNotFoundException;
import com.mohamed.coursemanagement.repository.CourseRepository;
import com.mohamed.coursemanagement.repository.InstructorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import com.mohamed.coursemanagement.mapper.CourseMapper;
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
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Spy
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Instructor instructor;
    private Course course;
    private LocalDateTime regStart;
    private LocalDateTime regEnd;

    @BeforeEach
    void setUp() {
        regStart = LocalDateTime.of(2026, 8, 1, 9, 0);
        regEnd = LocalDateTime.of(2026, 8, 15, 23, 59);

        instructor = Instructor.builder()
                .id(1L)
                .name("Dr. Ahmed Hassan")
                .email("ahmed@uni.edu")
                .build();

        course = Course.builder()
                .id(1L)
                .title("Spring Boot 101")
                .description("Intro to Spring Boot")
                .credits(3)
                .instructor(instructor)
                .registrationStartTime(regStart)
                .registrationEndTime(regEnd)
                .build();
    }

    @Test
    void create_savesCourseAndReturnsDto() {
        CourseRequestDto request = new CourseRequestDto("Spring Boot 101", "Intro to Spring Boot", 3, 1L, regStart, regEnd);
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponseDto result = courseService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Spring Boot 101");
        assertThat(result.instructorId()).isEqualTo(1L);
        assertThat(result.instructorName()).isEqualTo("Dr. Ahmed Hassan");
    }

    @Test
    void create_throwsNotFound_whenInstructorMissing() {
        CourseRequestDto request = new CourseRequestDto("Spring Boot 101", "Intro", 3, 99L, regStart, regEnd);
        when(instructorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Instructor not found");

        verify(courseRepository, never()).save(any());
    }

    @Test
    void create_throwsBadRequest_whenWindowEndsBeforeItStarts() {
        CourseRequestDto request = new CourseRequestDto("Spring Boot 101", "Intro", 3, 1L, regEnd, regStart);

        assertThatThrownBy(() -> courseService.create(request))
                .isInstanceOf(InvalidRegistrationWindowException.class)
                .hasMessageContaining("must be after");

        verify(courseRepository, never()).save(any());
    }

    @Test
    void create_returnsRegistrationWindowInResponse() {
        CourseRequestDto request = new CourseRequestDto("Spring Boot 101", "Intro to Spring Boot", 3, 1L, regStart, regEnd);
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponseDto result = courseService.create(request);

        assertThat(result.registrationStartTime()).isEqualTo(regStart);
        assertThat(result.registrationEndTime()).isEqualTo(regEnd);
    }

    @Test
    void getById_returnsDto_whenCourseExistsAndNotDeleted() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));

        CourseResponseDto result = courseService.getById(1L);

        assertThat(result.title()).isEqualTo("Spring Boot 101");
    }

    @Test
    void getById_throwsNotFound_whenCourseMissingOrDeleted() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAll_usesDeletedFalseFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(List.of(course), pageable, 1);
        when(courseRepository.findByDeletedFalse(pageable)).thenReturn(page);

        Page<CourseResponseDto> result = courseService.getAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(courseRepository).findByDeletedFalse(pageable);
    }

    @Test
    void getByInstructor_returnsCourses_whenInstructorExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(List.of(course), pageable, 1);
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByInstructorIdAndDeletedFalse(1L, pageable)).thenReturn(page);

        Page<CourseResponseDto> result = courseService.getByInstructor(1L, pageable);

        assertThat(result.getContent().getFirst().instructorId()).isEqualTo(1L);
    }

    @Test
    void getByInstructor_throwsNotFound_whenInstructorMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(instructorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getByInstructor(99L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_changesFieldsAndReturnsDto() {
        CourseRequestDto request = new CourseRequestDto("Spring Boot Advanced", "Deep dive", 4, 1L, regStart, regEnd);
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));

        CourseResponseDto result = courseService.update(1L, request);

        assertThat(result.title()).isEqualTo("Spring Boot Advanced");
        assertThat(result.credits()).isEqualTo(4);
    }

    @Test
    void update_reassignsInstructor_whenInstructorIdChanges() {
        Instructor newInstructor = Instructor.builder()
                .id(2L)
                .name("Dr. Sara Ali")
                .email("sara@uni.edu")
                .build();
        CourseRequestDto request = new CourseRequestDto("Spring Boot 101", "Intro", 3, 2L, regStart, regEnd);
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
        when(instructorRepository.findById(2L)).thenReturn(Optional.of(newInstructor));

        CourseResponseDto result = courseService.update(1L, request);

        assertThat(result.instructorId()).isEqualTo(2L);
        assertThat(result.instructorName()).isEqualTo("Dr. Sara Ali");
    }

    @Test
    void delete_softDeletes_insteadOfRemoving() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));

        courseService.delete(1L);

        assertThat(course.isDeleted()).isTrue();
        verify(courseRepository, never()).delete(any(Course.class));
        verify(courseRepository, never()).deleteById(any());
    }

    @Test
    void delete_throwsNotFound_whenCourseMissingOrAlreadyDeleted() {
        when(courseRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
