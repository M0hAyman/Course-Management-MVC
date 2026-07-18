package com.mohamed.coursemanagement.service;

import com.mohamed.coursemanagement.dto.EnrollmentRequestDto;
import com.mohamed.coursemanagement.dto.EnrollmentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService {

    EnrollmentResponseDto enroll(EnrollmentRequestDto request);

    void unenroll(Long enrollmentId);

    Page<EnrollmentResponseDto> getByStudent(Long studentId, Pageable pageable);

    Page<EnrollmentResponseDto> getByCourse(Long courseId, Pageable pageable);
}
