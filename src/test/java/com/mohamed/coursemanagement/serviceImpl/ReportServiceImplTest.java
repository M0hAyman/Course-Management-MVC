package com.mohamed.coursemanagement.serviceImpl;

import com.mohamed.coursemanagement.dto.CourseReportDto;
import com.mohamed.coursemanagement.entity.Course;
import com.mohamed.coursemanagement.entity.Instructor;
import com.mohamed.coursemanagement.repository.CourseRepository;
import com.mohamed.coursemanagement.repository.EnrollmentRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Spy
    private CourseMapper courseMapper;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Course course;

    @BeforeEach
    void setUp() {
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
                .build();
    }

    @Test
    void courseEnrollmentReport_returnsCountsPerCourse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(List.of(course), pageable, 1);
        when(courseRepository.findByDeletedFalse(pageable)).thenReturn(page);
        when(enrollmentRepository.countByCourseId(1L)).thenReturn(42L);

        Page<CourseReportDto> result = reportService.courseEnrollmentReport(pageable);

        CourseReportDto report = result.getContent().getFirst();
        assertThat(report.courseId()).isEqualTo(1L);
        assertThat(report.title()).isEqualTo("Spring Boot 101");
        assertThat(report.instructorName()).isEqualTo("Dr. Ahmed Hassan");
        assertThat(report.enrollmentCount()).isEqualTo(42L);
    }

    @Test
    void courseEnrollmentReport_excludesSoftDeletedCourses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(courseRepository.findByDeletedFalse(pageable)).thenReturn(emptyPage);

        Page<CourseReportDto> result = reportService.courseEnrollmentReport(pageable);

        assertThat(result.getTotalElements()).isZero();
    }
}
