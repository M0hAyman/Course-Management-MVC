package com.mohamed.coursemanagement.service;

import com.mohamed.coursemanagement.dto.CourseReportDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {

    Page<CourseReportDto> courseEnrollmentReport(Pageable pageable);
}
