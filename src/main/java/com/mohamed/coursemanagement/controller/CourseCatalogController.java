package com.mohamed.coursemanagement.controller;

import com.mohamed.coursemanagement.dto.CourseResponseDto;
import com.mohamed.coursemanagement.dto.EnrollmentRequestDto;
import com.mohamed.coursemanagement.dto.EnrollmentResponseDto;
import com.mohamed.coursemanagement.exception.DuplicateResourceException;
import com.mohamed.coursemanagement.exception.RegistrationClosedException;
import com.mohamed.coursemanagement.service.CourseService;
import com.mohamed.coursemanagement.service.EnrollmentService;
import com.mohamed.coursemanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseCatalogController {

    private final CourseService courseService;
    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<CourseCard> courses = courseService
                .getAll(PageRequest.of(page, 9, Sort.by("title")))
                .map(c -> new CourseCard(c, statusOf(c)));
        model.addAttribute("courses", courses);
        return "courses/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CourseResponseDto course = courseService.getById(id);
        model.addAttribute("course", course);
        model.addAttribute("status", statusOf(course));
        model.addAttribute("students",
                studentService.getAll(PageRequest.of(0, 100, Sort.by("name"))).getContent());
        model.addAttribute("enrollments",
                enrollmentService.getByCourse(id, PageRequest.of(0, 50, Sort.by("enrolledAt"))).getContent());
        return "courses/detail";
    }

    @PostMapping("/{id}/enroll")
    public String enroll(@PathVariable Long id, @RequestParam Long studentId, RedirectAttributes redirect) {
        try {
            EnrollmentResponseDto enrollment = enrollmentService.enroll(new EnrollmentRequestDto(studentId, id));
            redirect.addFlashAttribute("success",
                    enrollment.studentName() + " enrolled in " + enrollment.courseTitle());
        } catch (RegistrationClosedException | DuplicateResourceException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/courses/" + id;
    }

    static String statusOf(CourseResponseDto course) {
        LocalDateTime now = LocalDateTime.now();
        if (course.registrationStartTime() != null && now.isBefore(course.registrationStartTime())) {
            return "OPENS SOON";
        }
        if (course.registrationEndTime() != null && now.isAfter(course.registrationEndTime())) {
            return "CLOSED";
        }
        return "OPEN";
    }

    public record CourseCard(CourseResponseDto course, String status) {
    }
}
