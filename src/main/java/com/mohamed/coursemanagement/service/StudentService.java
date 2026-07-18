package com.mohamed.coursemanagement.service;

import com.mohamed.coursemanagement.dto.StudentRequestDto;
import com.mohamed.coursemanagement.dto.StudentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    StudentResponseDto create(StudentRequestDto request);

    StudentResponseDto getById(Long id);

    Page<StudentResponseDto> getAll(Pageable pageable);

    StudentResponseDto update(Long id, StudentRequestDto request);

    void delete(Long id);
}
