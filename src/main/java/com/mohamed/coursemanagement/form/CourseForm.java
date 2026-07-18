package com.mohamed.coursemanagement.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseForm {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Credits is required")
    @Positive(message = "Credits must be a positive number")
    private Integer credits;

    @NotNull(message = "Instructor is required")
    private Long instructorId;

    @NotNull(message = "Registration start time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime registrationStartTime;

    @NotNull(message = "Registration end time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime registrationEndTime;
}
