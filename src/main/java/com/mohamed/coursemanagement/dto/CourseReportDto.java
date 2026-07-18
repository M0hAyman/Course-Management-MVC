package com.mohamed.coursemanagement.dto;

import java.time.LocalDateTime;

public record CourseReportDto(
        Long courseId,
        String title,
        String instructorName,
        long enrollmentCount,
        LocalDateTime registrationStartTime,
        LocalDateTime registrationEndTime
) {
}
