package com.mohamed.coursemanagement.serviceImpl;

import com.mohamed.coursemanagement.dto.CourseRequestDto;
import com.mohamed.coursemanagement.dto.CourseResponseDto;
import com.mohamed.coursemanagement.entity.Course;
import com.mohamed.coursemanagement.entity.Instructor;
import com.mohamed.coursemanagement.exception.InvalidRegistrationWindowException;
import com.mohamed.coursemanagement.exception.ResourceNotFoundException;
import com.mohamed.coursemanagement.mapper.CourseMapper;
import com.mohamed.coursemanagement.repository.CourseRepository;
import com.mohamed.coursemanagement.repository.InstructorRepository;
import com.mohamed.coursemanagement.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final CourseMapper courseMapper;

    @Override
    public CourseResponseDto create(CourseRequestDto request) {
        validateRegistrationWindow(request);
        Instructor instructor = findInstructorOrThrow(request.instructorId());

        Course saved = courseRepository.save(courseMapper.toEntity(request, instructor));
        return courseMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDto getById(Long id) {
        return courseMapper.toDto(findCourseOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponseDto> getAll(Pageable pageable) {
        return courseRepository.findByDeletedFalse(pageable).map(courseMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponseDto> getByInstructor(Long instructorId, Pageable pageable) {
        findInstructorOrThrow(instructorId);
        return courseRepository.findByInstructorIdAndDeletedFalse(instructorId, pageable).map(courseMapper::toDto);
    }

    @Override
    public CourseResponseDto update(Long id, CourseRequestDto request) {
        validateRegistrationWindow(request);
        Course course = findCourseOrThrow(id);

        if (!course.getInstructor().getId().equals(request.instructorId())) {
            course.setInstructor(findInstructorOrThrow(request.instructorId()));
        }

        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setCredits(request.credits());
        course.setRegistrationStartTime(request.registrationStartTime());
        course.setRegistrationEndTime(request.registrationEndTime());

        return courseMapper.toDto(course);
    }

    @Override
    public void delete(Long id) {
        Course course = findCourseOrThrow(id);
        course.setDeleted(true);
    }

    private Course findCourseOrThrow(Long id) {
        return courseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    private Instructor findInstructorOrThrow(Long instructorId) {
        return instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + instructorId));
    }

    private void validateRegistrationWindow(CourseRequestDto request) {
        if (!request.registrationEndTime().isAfter(request.registrationStartTime())) {
            throw new InvalidRegistrationWindowException(
                    "Registration end time must be after the registration start time");
        }
    }
}
