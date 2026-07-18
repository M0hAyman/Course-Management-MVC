package com.mohamed.coursemanagement.serviceImpl;

import com.mohamed.coursemanagement.dto.InstructorRequestDto;
import com.mohamed.coursemanagement.dto.InstructorResponseDto;
import com.mohamed.coursemanagement.entity.Instructor;
import com.mohamed.coursemanagement.exception.DuplicateResourceException;
import com.mohamed.coursemanagement.exception.ResourceNotFoundException;
import com.mohamed.coursemanagement.repository.InstructorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import com.mohamed.coursemanagement.mapper.InstructorMapper;
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
class InstructorServiceImplTest {

    @Mock
    private InstructorRepository instructorRepository;

    @Spy
    private InstructorMapper instructorMapper;

    @InjectMocks
    private InstructorServiceImpl instructorService;

    private Instructor instructor;

    @BeforeEach
    void setUp() {
        instructor = Instructor.builder()
                .id(1L)
                .name("Dr. Ahmed Hassan")
                .email("ahmed@uni.edu")
                .build();
    }

    @Test
    void create_savesInstructorAndReturnsDto() {
        InstructorRequestDto request = new InstructorRequestDto("Dr. Ahmed Hassan", "ahmed@uni.edu");
        when(instructorRepository.existsByEmail("ahmed@uni.edu")).thenReturn(false);
        when(instructorRepository.save(any(Instructor.class))).thenReturn(instructor);

        InstructorResponseDto result = instructorService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Dr. Ahmed Hassan");
        assertThat(result.email()).isEqualTo("ahmed@uni.edu");
        verify(instructorRepository).save(any(Instructor.class));
    }

    @Test
    void create_throwsDuplicate_whenEmailAlreadyExists() {
        InstructorRequestDto request = new InstructorRequestDto("Dr. Ahmed Hassan", "ahmed@uni.edu");
        when(instructorRepository.existsByEmail("ahmed@uni.edu")).thenReturn(true);

        assertThatThrownBy(() -> instructorService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ahmed@uni.edu");

        verify(instructorRepository, never()).save(any());
    }

    @Test
    void getById_returnsDto_whenInstructorExists() {
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));

        InstructorResponseDto result = instructorService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Dr. Ahmed Hassan");
    }

    @Test
    void getById_throwsNotFound_whenInstructorMissing() {
        when(instructorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instructorService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_returnsPageOfDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Instructor> page = new PageImpl<>(List.of(instructor), pageable, 1);
        when(instructorRepository.findAll(pageable)).thenReturn(page);

        Page<InstructorResponseDto> result = instructorService.getAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Dr. Ahmed Hassan");
    }

    @Test
    void update_changesFieldsAndReturnsDto() {
        InstructorRequestDto request = new InstructorRequestDto("Dr. Ahmed H.", "ahmed.h@uni.edu");
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(instructorRepository.existsByEmail("ahmed.h@uni.edu")).thenReturn(false);

        InstructorResponseDto result = instructorService.update(1L, request);

        assertThat(result.name()).isEqualTo("Dr. Ahmed H.");
        assertThat(result.email()).isEqualTo("ahmed.h@uni.edu");
    }

    @Test
    void update_throwsDuplicate_whenNewEmailTakenByAnotherInstructor() {
        InstructorRequestDto request = new InstructorRequestDto("Dr. Ahmed Hassan", "taken@uni.edu");
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(instructorRepository.existsByEmail("taken@uni.edu")).thenReturn(true);

        assertThatThrownBy(() -> instructorService.update(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void update_allowsKeepingOwnEmail() {
        InstructorRequestDto request = new InstructorRequestDto("Dr. Ahmed H.", "ahmed@uni.edu");
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));

        InstructorResponseDto result = instructorService.update(1L, request);

        assertThat(result.name()).isEqualTo("Dr. Ahmed H.");
        verify(instructorRepository, never()).existsByEmail(any());
    }

    @Test
    void delete_removesInstructor_whenExists() {
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));

        instructorService.delete(1L);

        verify(instructorRepository).delete(instructor);
    }

    @Test
    void delete_throwsNotFound_whenInstructorMissing() {
        when(instructorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instructorService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(instructorRepository, never()).delete(any());
    }
}
