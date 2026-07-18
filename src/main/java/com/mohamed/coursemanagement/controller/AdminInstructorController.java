package com.mohamed.coursemanagement.controller;

import com.mohamed.coursemanagement.dto.InstructorRequestDto;
import com.mohamed.coursemanagement.dto.InstructorResponseDto;
import com.mohamed.coursemanagement.exception.DuplicateResourceException;
import com.mohamed.coursemanagement.form.InstructorForm;
import com.mohamed.coursemanagement.service.InstructorService;
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
@RequestMapping("/admin/instructors")
@RequiredArgsConstructor
public class AdminInstructorController {

    private final InstructorService instructorService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("instructors",
                instructorService.getAll(PageRequest.of(0, 50, Sort.by("name"))).getContent());
        return "admin/instructors/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new InstructorForm());
        model.addAttribute("formAction", "/admin/instructors");
        return "admin/instructors/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") InstructorForm form, BindingResult binding,
                         Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("formAction", "/admin/instructors");
            return "admin/instructors/form";
        }
        try {
            instructorService.create(new InstructorRequestDto(form.getName(), form.getEmail()));
            redirect.addFlashAttribute("success", "Instructor created");
            return "redirect:/admin/instructors";
        } catch (DuplicateResourceException ex) {
            binding.rejectValue("email", "duplicate", ex.getMessage());
            model.addAttribute("formAction", "/admin/instructors");
            return "admin/instructors/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        InstructorResponseDto instructor = instructorService.getById(id);
        model.addAttribute("form", new InstructorForm(instructor.name(), instructor.email()));
        model.addAttribute("formAction", "/admin/instructors/" + id + "/edit");
        return "admin/instructors/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") InstructorForm form,
                         BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("formAction", "/admin/instructors/" + id + "/edit");
            return "admin/instructors/form";
        }
        try {
            instructorService.update(id, new InstructorRequestDto(form.getName(), form.getEmail()));
            redirect.addFlashAttribute("success", "Instructor updated");
            return "redirect:/admin/instructors";
        } catch (DuplicateResourceException ex) {
            binding.rejectValue("email", "duplicate", ex.getMessage());
            model.addAttribute("formAction", "/admin/instructors/" + id + "/edit");
            return "admin/instructors/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        instructorService.delete(id);
        redirect.addFlashAttribute("success", "Instructor deleted");
        return "redirect:/admin/instructors";
    }
}
