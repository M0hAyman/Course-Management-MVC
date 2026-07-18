package com.mohamed.coursemanagement.mapper;

import com.mohamed.coursemanagement.dto.InstructorRequestDto;
import com.mohamed.coursemanagement.dto.InstructorResponseDto;
import com.mohamed.coursemanagement.entity.Instructor;
import org.springframework.stereotype.Component;

@Component
public class InstructorMapper {

    public InstructorResponseDto toDto(Instructor instructor) {
        return new InstructorResponseDto(instructor.getId(), instructor.getName(), instructor.getEmail());
    }

    public Instructor toEntity(InstructorRequestDto request) {
        return Instructor.builder()
                .name(request.name())
                .email(request.email())
                .build();
    }
}
