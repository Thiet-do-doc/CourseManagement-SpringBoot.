package vn.cmc.coursemanager.controller;
import vn.cmc.coursemanager.dto.CategoryForm;
import vn.cmc.coursemanager.exception.BusinessException;
import vn.cmc.coursemanager.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;
@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categories;
    public CategoryController(CategoryService categories) { this.categories=categories; }
    @GetMapping public String index(Model model) { model.addAttribute("categories", categories.all()); return "categories/index"; }
    @GetMapping("/create") public String create(Model model) { model.addAttribute("form", new CategoryForm()); return prepare(null,model); }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id, Model model) { model.addAttribute("form", categories.form(id)); return prepare(id,model); }
    @PostMapping("/create") public String createPost(@Valid @ModelAttribute("form") CategoryForm f, BindingResult errors, Model model, RedirectAttributes flash) { return save(null,f,errors,model,flash); }
    @PostMapping("/{id}/edit") public String editPost(@PathVariable Long id, @Valid @ModelAttribute("form") CategoryForm f, BindingResult errors, Model model, RedirectAttributes flash) {
        categories.detail(id); return save(id,f,errors,model,flash);
    }
    private String save(Long id, CategoryForm f, BindingResult errors, Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) return prepare(id,model);
        categories.save(id,f); flash.addFlashAttribute("success", "Đã lưu loại khóa học."); return "redirect:/categories";
    }
    private String prepare(Long id, Model model) { model.addAttribute("categoryId",id); return "categories/form"; }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id, RedirectAttributes flash) {
        try { categories.delete(id); flash.addFlashAttribute("success", "Đã xóa loại khóa học."); }
        catch (BusinessException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        catch (DataIntegrityViolationException ex) { flash.addFlashAttribute("error", "Loại này đang được sử dụng, không thể xóa."); }
        return "redirect:/categories";
    }
}
