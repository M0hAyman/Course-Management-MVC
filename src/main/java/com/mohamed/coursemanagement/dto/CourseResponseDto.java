package com.mohamed.coursemanagement.dto;

import java.time.LocalDateTime;

public record CourseResponseDto(
        Long id,
        String title,
        String description,
        Integer credits,
        Long instructorId,
        String instructorName,
        LocalDateTime registrationStartTime,
        LocalDateTime registrationEndTime
) {
}
