package com.mohamed.coursemanagement.mapper;

import com.mohamed.coursemanagement.dto.EnrollmentResponseDto;
import com.mohamed.coursemanagement.entity.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentResponseDto toDto(Enrollment enrollment) {
        return new EnrollmentResponseDto(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getEnrolledAt()
        );
    }
}
