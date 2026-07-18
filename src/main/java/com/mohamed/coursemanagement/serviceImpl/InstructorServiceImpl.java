package com.mohamed.coursemanagement.serviceImpl;

import com.mohamed.coursemanagement.dto.InstructorRequestDto;
import com.mohamed.coursemanagement.dto.InstructorResponseDto;
import com.mohamed.coursemanagement.entity.Instructor;
import com.mohamed.coursemanagement.exception.DuplicateResourceException;
import com.mohamed.coursemanagement.exception.ResourceNotFoundException;
import com.mohamed.coursemanagement.mapper.InstructorMapper;
import com.mohamed.coursemanagement.repository.InstructorRepository;
import com.mohamed.coursemanagement.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository instructorRepository;
    private final InstructorMapper instructorMapper;

    @Override
    public InstructorResponseDto create(InstructorRequestDto request) {
        if (instructorRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An instructor with email '" + request.email() + "' already exists");
        }

        Instructor saved = instructorRepository.save(instructorMapper.toEntity(request));
        return instructorMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorResponseDto getById(Long id) {
        return instructorMapper.toDto(findInstructorOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InstructorResponseDto> getAll(Pageable pageable) {
        return instructorRepository.findAll(pageable).map(instructorMapper::toDto);
    }

    @Override
    public InstructorResponseDto update(Long id, InstructorRequestDto request) {
        Instructor instructor = findInstructorOrThrow(id);

        if (!instructor.getEmail().equals(request.email()) && instructorRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An instructor with email '" + request.email() + "' already exists");
        }

        instructor.setName(request.name());
        instructor.setEmail(request.email());

        return instructorMapper.toDto(instructor);
    }

    @Override
    public void delete(Long id) {
        Instructor instructor = findInstructorOrThrow(id);
        instructorRepository.delete(instructor);
    }

    private Instructor findInstructorOrThrow(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + id));
    }
}
