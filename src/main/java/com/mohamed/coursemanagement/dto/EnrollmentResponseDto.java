package com.mohamed.coursemanagement.dto;

import java.time.LocalDateTime;

public record EnrollmentResponseDto(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseTitle,
        LocalDateTime enrolledAt
) {
}
