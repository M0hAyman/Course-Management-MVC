package com.mohamed.coursemanagement.mapper;

import com.mohamed.coursemanagement.dto.StudentRequestDto;
import com.mohamed.coursemanagement.dto.StudentResponseDto;
import com.mohamed.coursemanagement.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentResponseDto toDto(Student student) {
        return new StudentResponseDto(student.getId(), student.getName(), student.getEmail());
    }

    public Student toEntity(StudentRequestDto request) {
        return Student.builder()
                .name(request.name())
                .email(request.email())
                .build();
    }
}
