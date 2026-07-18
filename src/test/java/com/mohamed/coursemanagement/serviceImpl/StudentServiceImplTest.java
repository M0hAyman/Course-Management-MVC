package com.mohamed.coursemanagement.serviceImpl;

import com.mohamed.coursemanagement.dto.StudentRequestDto;
import com.mohamed.coursemanagement.dto.StudentResponseDto;
import com.mohamed.coursemanagement.entity.Student;
import com.mohamed.coursemanagement.exception.DuplicateResourceException;
import com.mohamed.coursemanagement.exception.ResourceNotFoundException;
import com.mohamed.coursemanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import com.mohamed.coursemanagement.mapper.StudentMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Spy
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L)
                .name("Mohamed Ayman")
                .email("mohamed@student.edu")
                .build();
    }

    @Test
    void create_savesStudentAndReturnsDto() {
        StudentRequestDto request = new StudentRequestDto("Mohamed Ayman", "mohamed@student.edu");
        when(studentRepository.existsByEmail("mohamed@student.edu")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponseDto result = studentService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Mohamed Ayman");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void create_throwsDuplicate_whenEmailAlreadyExists() {
        StudentRequestDto request = new StudentRequestDto("Mohamed Ayman", "mohamed@student.edu");
        when(studentRepository.existsByEmail("mohamed@student.edu")).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("mohamed@student.edu");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void getById_returnsDto_whenStudentExists() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentResponseDto result = studentService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("mohamed@student.edu");
    }

    @Test
    void getById_throwsNotFound_whenStudentMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_returnsPageOfDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = new PageImpl<>(List.of(student), pageable, 1);
        when(studentRepository.findAll(pageable)).thenReturn(page);

        Page<StudentResponseDto> result = studentService.getAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Mohamed Ayman");
    }

    @Test
    void update_changesFieldsAndReturnsDto() {
        StudentRequestDto request = new StudentRequestDto("Mohamed A.", "mohamed.a@student.edu");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail("mohamed.a@student.edu")).thenReturn(false);

        StudentResponseDto result = studentService.update(1L, request);

        assertThat(result.name()).isEqualTo("Mohamed A.");
        assertThat(result.email()).isEqualTo("mohamed.a@student.edu");
    }

    @Test
    void update_throwsDuplicate_whenNewEmailTakenByAnotherStudent() {
        StudentRequestDto request = new StudentRequestDto("Mohamed Ayman", "taken@student.edu");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail("taken@student.edu")).thenReturn(true);

        assertThatThrownBy(() -> studentService.update(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_removesStudent_whenExists() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.delete(1L);

        verify(studentRepository).delete(student);
    }

    @Test
    void delete_throwsNotFound_whenStudentMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(studentRepository, never()).delete(any());
    }
}
