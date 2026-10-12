package vn.cmc.coursemanager.controller;
import vn.cmc.coursemanager.dto.*;
import vn.cmc.coursemanager.exception.BusinessException;
import vn.cmc.coursemanager.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
@Controller
public class CourseController {
    private final CourseService courses;
    private final CategoryService categories;
    public CourseController(CourseService courses, CategoryService categories) { this.courses=courses; this.categories=categories; }
    @GetMapping("/") public String home() { return "redirect:/courses"; }
    @GetMapping("/courses")
    public String index(@Valid @ModelAttribute("filter") CourseFilter filter, BindingResult errors, Model model) {
        model.addAttribute("categories", categories.all());
        model.addAttribute("courses", List.of());
        if (!errors.hasErrors()) {
            try { model.addAttribute("courses", courses.search(filter)); }
            catch (BusinessException ex) { errors.rejectValue(ex.getField(), "business", ex.getMessage()); }
        }
        return "courses/index";
    }
    @GetMapping("/courses/create") public String create(Model model) {
        model.addAttribute("form", new CourseForm()); return prepareForm(null, model);
    }
    @GetMapping("/courses/{id}/edit") public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", courses.form(id)); return prepareForm(id, model);
    }
    @PostMapping("/courses/create") public String createPost(@Valid @ModelAttribute("form") CourseForm form, BindingResult errors, Model model, RedirectAttributes flash) {
        return save(null, form, errors, model, flash);
    }
    @PostMapping("/courses/{id}/edit") public String editPost(@PathVariable Long id, @Valid @ModelAttribute("form") CourseForm form, BindingResult errors, Model model, RedirectAttributes flash) {
        courses.detail(id); return save(id, form, errors, model, flash);
    }
    private String save(Long id, CourseForm form, BindingResult errors, Model model, RedirectAttributes flash) {
        if (!errors.hasErrors()) {
            try { Long saved=courses.save(id,form); flash.addFlashAttribute("success", "Đã lưu khóa học."); return "redirect:/courses/"+saved; }
            catch (BusinessException ex) { errors.rejectValue(ex.getField(), "business", ex.getMessage()); }
            catch (DataIntegrityViolationException ex) { errors.reject("conflict", "Không thể lưu. Hãy kiểm tra tên trùng hoặc loại khóa học đã bị thay đổi."); }
        }
        return prepareForm(id, model);
    }
    private String prepareForm(Long id, Model model) {
        model.addAttribute("courseId", id); model.addAttribute("categories", categories.all()); return "courses/form";
    }
    @GetMapping("/courses/{id}") public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("imageForm", new ImageForm()); return prepareDetail(id, model);
    }
    private String prepareDetail(Long id, Model model) { model.addAttribute("course", courses.detail(id)); return "courses/detail"; }
    @PostMapping("/courses/{id}/delete") public String delete(@PathVariable Long id, RedirectAttributes flash) {
        courses.delete(id); flash.addFlashAttribute("success", "Đã xóa khóa học và các ảnh liên quan."); return "redirect:/courses";
    }
    @PostMapping("/courses/{id}/images") public String addImage(@PathVariable Long id, @Valid @ModelAttribute("imageForm") ImageForm form, BindingResult errors, Model model, RedirectAttributes flash) {
        courses.detail(id);
        if (!errors.hasErrors()) {
            try { courses.addImage(id,form); flash.addFlashAttribute("success", "Đã thêm ảnh."); return "redirect:/courses/"+id; }
            catch (BusinessException ex) { errors.rejectValue(ex.getField(), "business", ex.getMessage()); }
        }
        return prepareDetail(id, model);
    }
    @PostMapping("/courses/{id}/images/{imageId}/thumbnail") public String thumbnail(@PathVariable Long id, @PathVariable Long imageId, RedirectAttributes flash) {
        courses.setThumbnail(id,imageId); flash.addFlashAttribute("success", "Đã đổi ảnh đại diện."); return "redirect:/courses/"+id;
    }
    @PostMapping("/courses/{id}/images/{imageId}/delete") public String deleteImage(@PathVariable Long id, @PathVariable Long imageId, RedirectAttributes flash) {
        courses.deleteImage(id,imageId); flash.addFlashAttribute("success", "Đã xóa ảnh."); return "redirect:/courses/"+id;
    }
}
