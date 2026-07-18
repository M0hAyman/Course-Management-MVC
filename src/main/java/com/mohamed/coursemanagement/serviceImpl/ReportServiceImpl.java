package com.mohamed.coursemanagement.serviceImpl;

import com.mohamed.coursemanagement.dto.CourseReportDto;
import com.mohamed.coursemanagement.mapper.CourseMapper;
import com.mohamed.coursemanagement.repository.CourseRepository;
import com.mohamed.coursemanagement.repository.EnrollmentRepository;
import com.mohamed.coursemanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseMapper courseMapper;

    @Override
    public Page<CourseReportDto> courseEnrollmentReport(Pageable pageable) {
        return courseRepository.findByDeletedFalse(pageable)
                .map(course -> courseMapper.toReportDto(course, enrollmentRepository.countByCourseId(course.getId())));
    }
}
