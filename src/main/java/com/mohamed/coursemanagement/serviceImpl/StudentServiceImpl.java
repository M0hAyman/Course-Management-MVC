package com.mohamed.coursemanagement.serviceImpl;

import com.mohamed.coursemanagement.dto.StudentRequestDto;
import com.mohamed.coursemanagement.dto.StudentResponseDto;
import com.mohamed.coursemanagement.entity.Student;
import com.mohamed.coursemanagement.exception.DuplicateResourceException;
import com.mohamed.coursemanagement.exception.ResourceNotFoundException;
import com.mohamed.coursemanagement.mapper.StudentMapper;
import com.mohamed.coursemanagement.repository.StudentRepository;
import com.mohamed.coursemanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    @Override
    public StudentResponseDto create(StudentRequestDto request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A student with email '" + request.email() + "' already exists");
        }

        Student saved = studentRepository.save(studentMapper.toEntity(request));
        return studentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDto getById(Long id) {
        return studentMapper.toDto(findStudentOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponseDto> getAll(Pageable pageable) {
        return studentRepository.findAll(pageable).map(studentMapper::toDto);
    }

    @Override
    public StudentResponseDto update(Long id, StudentRequestDto request) {
        Student student = findStudentOrThrow(id);

        if (!student.getEmail().equals(request.email()) && studentRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A student with email '" + request.email() + "' already exists");
        }

        student.setName(request.name());
        student.setEmail(request.email());

        return studentMapper.toDto(student);
    }

    @Override
    public void delete(Long id) {
        Student student = findStudentOrThrow(id);
        studentRepository.delete(student);
    }

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }
}
