package com.mohamed.coursemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record CourseRequestDto(

        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Credits is required")
        @Positive(message = "Credits must be a positive number")
        Integer credits,

        @NotNull(message = "Instructor id is required")
        Long instructorId,

        @NotNull(message = "Registration start time is required")
        LocalDateTime registrationStartTime,

        @NotNull(message = "Registration end time is required")
        LocalDateTime registrationEndTime
) {
}
