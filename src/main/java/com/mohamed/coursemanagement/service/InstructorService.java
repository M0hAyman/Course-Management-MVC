package com.mohamed.coursemanagement.service;

import com.mohamed.coursemanagement.dto.InstructorRequestDto;
import com.mohamed.coursemanagement.dto.InstructorResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InstructorService {

    InstructorResponseDto create(InstructorRequestDto request);

    InstructorResponseDto getById(Long id);

    Page<InstructorResponseDto> getAll(Pageable pageable);

    InstructorResponseDto update(Long id, InstructorRequestDto request);

    void delete(Long id);
}
