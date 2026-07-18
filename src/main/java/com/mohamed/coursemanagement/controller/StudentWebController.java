package com.mohamed.coursemanagement.controller;

import com.mohamed.coursemanagement.dto.StudentRequestDto;
import com.mohamed.coursemanagement.dto.StudentResponseDto;
import com.mohamed.coursemanagement.exception.DuplicateResourceException;
import com.mohamed.coursemanagement.form.StudentForm;
import com.mohamed.coursemanagement.service.EnrollmentService;
import com.mohamed.coursemanagement.service.StudentService;
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
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentWebController {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("students",
                studentService.getAll(PageRequest.of(0, 50, Sort.by("name"))).getContent());
        return "students/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new StudentForm());
        model.addAttribute("formAction", "/students");
        return "students/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") StudentForm form, BindingResult binding,
                         Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("formAction", "/students");
            return "students/form";
        }
        try {
            StudentResponseDto created = studentService.create(new StudentRequestDto(form.getName(), form.getEmail()));
            redirect.addFlashAttribute("success", "Student '" + created.name() + "' registered");
            return "redirect:/students/" + created.id();
        } catch (DuplicateResourceException ex) {
            binding.rejectValue("email", "duplicate", ex.getMessage());
            model.addAttribute("formAction", "/students");
            return "students/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getById(id));
        model.addAttribute("enrollments",
                enrollmentService.getByStudent(id, PageRequest.of(0, 50, Sort.by("enrolledAt"))).getContent());
        return "students/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        StudentResponseDto student = studentService.getById(id);
        model.addAttribute("form", new StudentForm(student.name(), student.email()));
        model.addAttribute("formAction", "/students/" + id + "/edit");
        return "students/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") StudentForm form,
                         BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("formAction", "/students/" + id + "/edit");
            return "students/form";
        }
        try {
            studentService.update(id, new StudentRequestDto(form.getName(), form.getEmail()));
            redirect.addFlashAttribute("success", "Student updated");
            return "redirect:/students/" + id;
        } catch (DuplicateResourceException ex) {
            binding.rejectValue("email", "duplicate", ex.getMessage());
            model.addAttribute("formAction", "/students/" + id + "/edit");
            return "students/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        studentService.delete(id);
        redirect.addFlashAttribute("success", "Student deleted");
        return "redirect:/students";
    }

    @PostMapping("/{studentId}/unenroll/{enrollmentId}")
    public String unenroll(@PathVariable Long studentId, @PathVariable Long enrollmentId,
                           RedirectAttributes redirect) {
        enrollmentService.unenroll(enrollmentId);
        redirect.addFlashAttribute("success", "Unenrolled successfully");
        return "redirect:/students/" + studentId;
    }
}
