package com.mohamed.coursemanagement.mapper;

import com.mohamed.coursemanagement.dto.CourseReportDto;
import com.mohamed.coursemanagement.dto.CourseRequestDto;
import com.mohamed.coursemanagement.dto.CourseResponseDto;
import com.mohamed.coursemanagement.entity.Course;
import com.mohamed.coursemanagement.entity.Instructor;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseResponseDto toDto(Course course) {
        return new CourseResponseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCredits(),
                course.getInstructor().getId(),
                course.getInstructor().getName(),
                course.getRegistrationStartTime(),
                course.getRegistrationEndTime()
        );
    }

    public Course toEntity(CourseRequestDto request, Instructor instructor) {
        return Course.builder()
                .title(request.title())
                .description(request.description())
                .credits(request.credits())
                .instructor(instructor)
                .registrationStartTime(request.registrationStartTime())
                .registrationEndTime(request.registrationEndTime())
                .build();
    }

    public CourseReportDto toReportDto(Course course, long enrollmentCount) {
        return new CourseReportDto(
                course.getId(),
                course.getTitle(),
                course.getInstructor().getName(),
                enrollmentCount,
                course.getRegistrationStartTime(),
                course.getRegistrationEndTime()
        );
    }
}
