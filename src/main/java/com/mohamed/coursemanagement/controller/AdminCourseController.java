package com.mohamed.coursemanagement.controller;

import com.mohamed.coursemanagement.dto.CourseRequestDto;
import com.mohamed.coursemanagement.dto.CourseResponseDto;
import com.mohamed.coursemanagement.exception.InvalidRegistrationWindowException;
import com.mohamed.coursemanagement.form.CourseForm;
import com.mohamed.coursemanagement.service.CourseService;
import com.mohamed.coursemanagement.service.InstructorService;
import com.mohamed.coursemanagement.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;
    private final InstructorService instructorService;
    private final ReportService reportService;

    @GetMapping("/courses")
    public String list(Model model) {
        model.addAttribute("courses",
                courseService.getAll(PageRequest.of(0, 50, Sort.by("title"))).getContent());
        return "admin/courses/list";
    }

    @GetMapping("/courses/new")
    public String createForm(Model model) {
        model.addAttribute("form", new CourseForm());
        prepareFormModel(model, "/admin/courses");
        return "admin/courses/form";
    }

    @PostMapping("/courses")
    public String create(@Valid @ModelAttribute("form") CourseForm form, BindingResult binding,
                         Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            prepareFormModel(model, "/admin/courses");
            return "admin/courses/form";
        }
        try {
            courseService.create(toDto(form));
            redirect.addFlashAttribute("success", "Course created");
            return "redirect:/admin/courses";
        } catch (InvalidRegistrationWindowException ex) {
            binding.rejectValue("registrationEndTime", "window", ex.getMessage());
            prepareFormModel(model, "/admin/courses");
            return "admin/courses/form";
        }
    }

    @GetMapping("/courses/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CourseResponseDto course = courseService.getById(id);
        model.addAttribute("form", new CourseForm(
                course.title(), course.description(), course.credits(), course.instructorId(),
                course.registrationStartTime(), course.registrationEndTime()));
        prepareFormModel(model, "/admin/courses/" + id + "/edit");
        return "admin/courses/form";
    }

    @PostMapping("/courses/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") CourseForm form,
                         BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            prepareFormModel(model, "/admin/courses/" + id + "/edit");
            return "admin/courses/form";
        }
        try {
            courseService.update(id, toDto(form));
            redirect.addFlashAttribute("success", "Course updated");
            return "redirect:/admin/courses";
        } catch (InvalidRegistrationWindowException ex) {
            binding.rejectValue("registrationEndTime", "window", ex.getMessage());
            prepareFormModel(model, "/admin/courses/" + id + "/edit");
            return "admin/courses/form";
        }
    }

    @PostMapping("/courses/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        courseService.delete(id);
        redirect.addFlashAttribute("success", "Course deleted (soft delete: enrollment history kept)");
        return "redirect:/admin/courses";
    }

    @GetMapping("/reports")
    public String report(Model model) {
        model.addAttribute("report",
                reportService.courseEnrollmentReport(PageRequest.of(0, 50, Sort.by("title"))).getContent());
        return "admin/report";
    }

    private void prepareFormModel(Model model, String formAction) {
        model.addAttribute("formAction", formAction);
        model.addAttribute("instructors",
                instructorService.getAll(PageRequest.of(0, 100, Sort.by("name"))).getContent());
    }

    private CourseRequestDto toDto(CourseForm form) {
        return new CourseRequestDto(form.getTitle(), form.getDescription(), form.getCredits(),
                form.getInstructorId(), form.getRegistrationStartTime(), form.getRegistrationEndTime());
    }
}
