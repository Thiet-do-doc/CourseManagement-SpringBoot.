package vn.cmc.coursemanager.service;
import vn.cmc.coursemanager.dto.CategoryForm;
import vn.cmc.coursemanager.entity.CourseCategory;
import vn.cmc.coursemanager.exception.*;
import vn.cmc.coursemanager.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import java.util.List;
@Service
@Transactional(readOnly=true)
public class CategoryService {
    private final CourseCategoryRepository categories;
    private final CourseRepository courses;
    public CategoryService(CourseCategoryRepository categories, CourseRepository courses) { this.categories=categories; this.courses=courses; }
    public List<CourseCategory> all() { return categories.findAll(Sort.by("name")); }
    public CourseCategory detail(Long id) {
        return categories.findById(id).orElseThrow(() -> new NotFoundException("Loại khóa học không tồn tại."));
    }
    public CategoryForm form(Long id) {
        CourseCategory c=detail(id); CategoryForm f=new CategoryForm(); f.setName(c.getName()); f.setDescription(c.getDescription()); return f;
    }
    @Transactional
    public void save(Long id, CategoryForm f) {
        CourseCategory c=id==null ? new CourseCategory() : categories.findLockedById(id)
            .orElseThrow(() -> new NotFoundException("Loại khóa học không tồn tại."));
        c.setName(f.getName().trim()); c.setDescription(f.getDescription()); categories.saveAndFlush(c);
    }
    @Transactional
    public void delete(Long id) {
        CourseCategory c=categories.findLockedById(id).orElseThrow(() -> new NotFoundException("Loại khóa học không tồn tại."));
        if (courses.existsByCategoryId(id)) throw new BusinessException("categoryId", "Không thể xóa loại đang có khóa học. Hãy chuyển hoặc xóa các khóa học trước.");
        categories.delete(c); categories.flush();
    }
}
